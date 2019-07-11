package io.plastique.deviations

import androidx.room.RoomDatabase
import com.github.technoir42.rxjava2.extensions.mapError
import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import io.plastique.api.ApiException
import io.plastique.api.common.ErrorType
import io.plastique.api.deviations.DeviationDto
import io.plastique.api.deviations.DeviationService
import io.plastique.api.deviations.ImageDto
import io.plastique.api.deviations.VideoDto
import io.plastique.core.cache.CacheEntry
import io.plastique.core.cache.CacheEntryRepository
import io.plastique.core.cache.CacheHelper
import io.plastique.core.cache.MetadataValidatingCacheEntryChecker
import io.plastique.core.paging.Cursor
import io.plastique.core.paging.PagedData
import io.plastique.core.time.TimeProvider
import io.plastique.users.UserRepository
import io.plastique.users.toUser
import io.plastique.util.RxRoom
import io.plastique.util.Size
import io.reactivex.Observable
import io.reactivex.Single
import org.threeten.bp.Duration
import org.threeten.bp.ZoneId
import javax.inject.Inject

class DeviationRepositoryImpl @Inject constructor(
    private val database: RoomDatabase,
    private val deviationDao: DeviationDao,
    private val deviationService: DeviationService,
    private val cacheEntryRepository: CacheEntryRepository,
    private val fetcherFactory: DeviationFetcherFactory,
    private val timeProvider: TimeProvider,
    private val userRepository: UserRepository
) : DeviationRepository {

    fun getDeviations(params: FetchParams): Observable<PagedData<List<Deviation>, Cursor>> {
        val fetcher = fetcherFactory.createFetcher(params)
        val metadataSerializer = fetcher.createMetadataSerializer()
        val cacheEntryChecker = MetadataValidatingCacheEntryChecker(timeProvider, CACHE_DURATION) { serializedMetadata ->
            val metadata = metadataSerializer.deserialize(serializedMetadata)
            metadata?.params == params
        }
        val cacheKey = fetcher.getCacheKey(params)
        return CacheHelper(cacheEntryRepository, cacheEntryChecker).createObservable(
            cacheKey = cacheKey,
            cachedData = getDeviationsFromDb(cacheKey, params, metadataSerializer),
            updater = fetch(fetcher, cacheKey, params).ignoreElement())
    }

    fun fetch(params: FetchParams, cursor: Cursor? = null): Single<Optional<Cursor>> {
        val fetcher = fetcherFactory.createFetcher(params)
        return fetch(fetcher, fetcher.getCacheKey(params), params, cursor)
    }

    override fun put(deviations: Collection<DeviationDto>) {
        if (deviations.isEmpty()) {
            return
        }

        val deviationEntities = deviations.map { it.toDeviationEntity() }
        val dailyDeviationEntities = deviations.mapNotNull { it.dailyDeviation?.toDailyDeviationEntity(it.id) }
        val deviationsWithoutDailyIds = deviations.asSequence()
            .filter { it.dailyDeviation == null }
            .map { it.id }
            .toSet()

        val imageEntities = mutableListOf<DeviationImageEntity>()
        val videoEntities = mutableListOf<DeviationVideoEntity>()
        deviations.forEach { deviation ->
            imageEntities += deviation.thumbnails.asSequence()
                .map { createImageEntity(deviation.id, DeviationImageType.Thumbnail, it) }
                .distinctBy { it.size } // Ignore duplicates with the same dimensions

            deviation.preview?.let { imageEntities += createImageEntity(deviation.id, DeviationImageType.Preview, it) }
            deviation.content?.let { imageEntities += createImageEntity(deviation.id, DeviationImageType.Content, it) }

            videoEntities += deviation.videos.asSequence().map { it.toVideoEntity(deviation.id) }
        }

        val users = deviations.asSequence()
            .flatMap { sequenceOf(it.author, it.dailyDeviation?.giver) }
            .filterNotNull()
            .distinctBy { user -> user.id }
            .toList()

        database.runInTransaction {
            userRepository.put(users)
            deviationDao.insertOrUpdate(deviationEntities)

            if (deviationsWithoutDailyIds.isNotEmpty()) {
                deviationDao.deleteDailyDeviations(deviationsWithoutDailyIds)
            }
            if (dailyDeviationEntities.isNotEmpty()) {
                deviationDao.insertOrUpdateDailyDeviations(dailyDeviationEntities)
            }

            deviationDao.replaceImages(imageEntities)
            deviationDao.replaceVideos(videoEntities)
        }
    }

    private fun fetch(fetcher: DeviationFetcher<FetchParams, Cursor>, cacheKey: String, params: FetchParams, cursor: Cursor? = null): Single<Optional<Cursor>> {
        return fetcher.fetch(params, cursor)
            .map { fetchResult ->
                val cacheMetadata = DeviationCacheMetadata(params, fetchResult.nextCursor)
                val metadataSerializer = fetcher.createMetadataSerializer()
                val cacheEntry = CacheEntry(key = cacheKey, timestamp = timeProvider.currentInstant, metadata = metadataSerializer.serialize(cacheMetadata))
                persist(cacheEntry = cacheEntry, deviations = fetchResult.deviations, replaceExisting = fetchResult.replaceExisting)
                cacheMetadata.nextCursor.toOptional()
            }
    }

    private fun getDeviationsFromDb(
        key: String,
        params: FetchParams,
        metadataSerializer: DeviationCacheMetadataSerializer
    ): Observable<PagedData<List<Deviation>, Cursor>> {
        return RxRoom.createObservable(database, TABLE_NAMES) {
            val deviationsWithRelations = deviationDao.getDeviationsByKey(key)
            val deviations = combineAndFilter(deviationsWithRelations, params)
            val nextCursor = getNextCursor(key, metadataSerializer)
            PagedData(deviations, nextCursor)
        }.distinctUntilChanged()
    }

    private fun getNextCursor(cacheKey: String, metadataSerializer: DeviationCacheMetadataSerializer): Cursor? {
        val cacheEntry = cacheEntryRepository.getEntryByKey(cacheKey)
        val metadata = cacheEntry?.metadata?.let { metadataSerializer.deserialize(it) }
        return metadata?.nextCursor
    }

    private fun combineAndFilter(deviationsWithRelations: List<DeviationEntityWithRelations>, params: FetchParams): List<Deviation> {
        return deviationsWithRelations
            .asSequence()
            .map { it.toDeviation(timeProvider.timeZone) }
            .filter { matchesParams(it, params) }
            .toList()
    }

    private fun matchesParams(deviation: Deviation, params: FetchParams): Boolean {
        return (params.showLiterature || deviation.data !is Deviation.Data.Literature) &&
                (params.showMatureContent || !deviation.properties.isMature)
    }

    private fun persist(cacheEntry: CacheEntry, deviations: List<DeviationDto>, replaceExisting: Boolean) {
        database.runInTransaction {
            put(deviations)
            cacheEntryRepository.setEntry(cacheEntry)

            val startIndex = if (replaceExisting) {
                deviationDao.deleteLinksByKey(cacheEntry.key)
                1
            } else {
                deviationDao.getMaxOrder(cacheEntry.key) + 1
            }

            val links = deviations.mapIndexed { index, deviation ->
                DeviationLinkage(key = cacheEntry.key, deviationId = deviation.id, order = startIndex + index)
            }
            deviationDao.insertLinks(links)
        }
    }

    override fun getDeviationById(deviationId: String): Observable<Deviation> {
        return getDeviationByIdFromDb(deviationId)
            .switchIfEmpty(getDeviationByIdFromServer(deviationId)
                .flatMapObservable { getDeviationByIdFromDb(deviationId) })
    }

    private fun getDeviationByIdFromDb(deviationId: String): Observable<Deviation> {
        return deviationDao.getDeviationById(deviationId)
            .takeWhile { it.isNotEmpty() }
            .map { it.first().toDeviation(timeProvider.timeZone) }
            .distinctUntilChanged()
    }

    private fun getDeviationByIdFromServer(deviationId: String): Single<DeviationDto> {
        return deviationService.getDeviationById(deviationId)
            .doOnSuccess { deviation -> put(listOf(deviation)) }
            .mapError { error ->
                if (error is ApiException && error.errorData.type == ErrorType.InvalidRequest) {
                    DeviationNotFoundException(deviationId, error)
                } else {
                    error
                }
            }
    }

    override fun getDeviationTitleById(deviationId: String): Single<String> {
        return deviationDao.getDeviationTitleById(deviationId)
            .switchIfEmpty(getDeviationByIdFromServer(deviationId)
                .map { deviation -> deviation.title })
    }

    companion object {
        private val CACHE_DURATION = Duration.ofHours(1)
        private val TABLE_NAMES = arrayOf("users", "daily_deviations", "deviation_images", "deviation_videos", "deviations", "deviation_linkage")
    }
}

private fun DeviationDto.toDeviationEntity(): DeviationEntity = DeviationEntity(
    id = id,
    title = title,
    url = url,
    categoryPath = categoryPath,
    publishTime = publishTime,
    authorId = author.id,
    excerpt = excerpt,
    properties = DeviationEntity.Properties(
        isDownloadable = isDownloadable,
        isFavorite = isFavorite,
        isMature = isMature,
        allowsComments = allowsComments,
        downloadFileSize = downloadFileSize),
    stats = DeviationEntity.Stats(comments = stats.comments, favorites = stats.favorites))

private fun DeviationDto.DailyDeviation.toDailyDeviationEntity(deviationId: String): DailyDeviationEntity =
    DailyDeviationEntity(deviationId = deviationId, body = body, date = date, giverId = giver.id)

fun DeviationEntityWithRelations.toDeviation(timeZone: ZoneId): Deviation {
    val data = when {
        deviation.excerpt != null ->
            Deviation.Data.Literature(excerpt = deviation.excerpt!!)

        videos.isNotEmpty() -> {
            val preview = images.asSequence()
                .filter { it.type == DeviationImageType.Preview }
                .map { it.toImageInfo() }
                .first()

            val thumbnails = images.asSequence()
                .filter { it.type == DeviationImageType.Thumbnail }
                .map { it.toImageInfo() }
                .sortedBy { it.size.width }
                .toList()

            val videos = videos.asSequence()
                .map { it.toVideoInfo() }
                .sortedBy { it.quality }
                .toList()

            Deviation.Data.Video(
                thumbnails = thumbnails,
                preview = preview,
                videos = videos)
        }

        else -> {
            val content = images.asSequence()
                .filter { it.type == DeviationImageType.Content }
                .map { it.toImageInfo() }
                .first()

            val preview = images.asSequence()
                .filter { it.type == DeviationImageType.Preview }
                .map { it.toImageInfo() }
                .first()

            val thumbnails = images.asSequence()
                .filter { it.type == DeviationImageType.Thumbnail }
                .map { it.toImageInfo() }
                .sortedBy { it.size.width }
                .toList()

            Deviation.Data.Image(
                content = content,
                preview = preview,
                thumbnails = thumbnails)
        }
    }

    return Deviation(
        id = deviation.id,
        title = deviation.title,
        url = deviation.url,
        categoryPath = deviation.categoryPath,
        publishTime = deviation.publishTime.atZone(timeZone),
        author = author.toUser(),
        data = data,
        properties = deviation.properties.toDeviationProperties(),
        stats = deviation.stats.toDeviationStats(),
        dailyDeviation = dailyDeviation?.toDailyDeviation())
}

private fun DeviationEntity.Properties.toDeviationProperties(): Deviation.Properties = Deviation.Properties(
    isDownloadable = isDownloadable,
    isFavorite = isFavorite,
    isMature = isMature,
    allowsComments = allowsComments,
    downloadFileSize = downloadFileSize)

private fun DeviationEntity.Stats.toDeviationStats(): Deviation.Stats = Deviation.Stats(comments = comments, favorites = favorites)

private fun DailyDeviationEntityWithRelations.toDailyDeviation(): Deviation.DailyDeviation = Deviation.DailyDeviation(
    body = dailyDeviation.body,
    date = dailyDeviation.date,
    giver = giver.toUser())

private fun DeviationImageEntity.toImageInfo(): Deviation.ImageInfo = Deviation.ImageInfo(size = size, url = url)

private fun createImageEntity(deviationId: String, type: DeviationImageType, imageDto: ImageDto): DeviationImageEntity {
    val size = Size(imageDto.width, imageDto.height)
    val id = when (type) {
        DeviationImageType.Content, DeviationImageType.Preview -> "$deviationId-${type.id}"
        DeviationImageType.Thumbnail -> "$deviationId-${type.id}-$size"
    }
    return DeviationImageEntity(id = id, deviationId = deviationId, type = type, size = size, url = imageDto.url)
}

private fun VideoDto.toVideoEntity(deviationId: String): DeviationVideoEntity = DeviationVideoEntity(
    deviationId = deviationId,
    quality = quality,
    url = url,
    duration = Duration.ofSeconds(duration.toLong()),
    fileSize = fileSize)

private fun DeviationVideoEntity.toVideoInfo(): Deviation.VideoInfo =
    Deviation.VideoInfo(duration = duration, quality = quality, url = url)
