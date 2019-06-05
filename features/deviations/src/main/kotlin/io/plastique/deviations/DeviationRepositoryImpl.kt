package io.plastique.deviations

import androidx.room.RoomDatabase
import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import com.sch.rxjava2.extensions.mapError
import io.plastique.api.ApiException
import io.plastique.api.common.ErrorType
import io.plastique.api.deviations.DeviationDto
import io.plastique.api.deviations.DeviationService
import io.plastique.api.deviations.ImageDto
import io.plastique.core.cache.CacheEntry
import io.plastique.core.cache.CacheEntryRepository
import io.plastique.core.cache.CacheHelper
import io.plastique.core.cache.MetadataValidatingCacheEntryChecker
import io.plastique.core.paging.Cursor
import io.plastique.core.paging.PagedData
import io.plastique.users.UserEntity
import io.plastique.users.UserRepository
import io.plastique.users.toUser
import io.plastique.util.RxRoom
import io.plastique.util.Size
import io.plastique.util.TimeProvider
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

        val users = deviations.asSequence()
            .flatMap { sequenceOf(it.author, it.dailyDeviation?.giver) }
            .filterNotNull()
            .distinctBy { user -> user.id }
            .toList()
        val deviationEntities = deviations.map { deviation -> deviation.toDeviationEntity() }

        val imageEntities = mutableListOf<DeviationImageEntity>()
        deviations.forEach { deviation ->
            imageEntities += deviation.thumbnails.asSequence()
                .map { createImageEntity(deviation.id, DeviationImageType.Thumbnail, it) }
                .distinctBy { it.size } // Ignore duplicates with the same dimensions

            deviation.preview?.let { imageEntities += createImageEntity(deviation.id, DeviationImageType.Preview, it) }
            deviation.content?.let { imageEntities += createImageEntity(deviation.id, DeviationImageType.Content, it) }
        }

        database.runInTransaction {
            userRepository.put(users)
            deviationDao.insertOrUpdate(deviationEntities)
            deviationDao.replaceImages(imageEntities)
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
        return RxRoom.createObservable(database, arrayOf("users", "deviation_images", "deviations", "deviation_linkage")) {
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
            .map { it.toDeviation() }
            .filter { matchesParams(it, params) }
            .toList()
    }

    private fun matchesParams(deviation: Deviation, params: FetchParams): Boolean {
        return (!deviation.isLiterature || params.showLiterature) &&
                (!deviation.properties.isMature || params.showMatureContent)
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
            .map { it.first().toDeviation() }
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
    properties = DeviationPropertiesEntity(
        isDownloadable = isDownloadable,
        isFavorite = isFavorite,
        isMature = isMature,
        allowsComments = allowsComments,
        downloadFileSize = downloadFileSize),
    stats = DeviationStatsEntity(comments = stats.comments, favorites = stats.favorites),
    dailyDeviation = dailyDeviation?.toDailyDeviationEntity())

private fun DeviationDto.DailyDeviation.toDailyDeviationEntity(): DailyDeviationEntity =
    DailyDeviationEntity(body = body, date = date, giverId = giver.id)

fun DeviationEntityWithRelations.toDeviation(): Deviation = Deviation(
    id = deviation.id,
    title = deviation.title,
    url = deviation.url,
    categoryPath = deviation.categoryPath,
    publishTime = deviation.publishTime.atZone(ZoneId.systemDefault()),
    author = author.first().toUser(),
    properties = deviation.properties.toDeviationProperties(),
    stats = Deviation.Stats(comments = deviation.stats.comments, favorites = deviation.stats.favorites),
    dailyDeviation = deviation.dailyDeviation?.toDailyDeviation(dailyDeviationGiver.first()),

    content = images.asSequence()
        .filter { it.type == DeviationImageType.Content }
        .map { it.toImageInfo() }
        .firstOrNull(),
    preview = images.asSequence()
        .filter { it.type == DeviationImageType.Preview }
        .map { it.toImageInfo() }
        .firstOrNull(),
    thumbnails = images.asSequence()
        .filter { it.type == DeviationImageType.Thumbnail }
        .map { it.toImageInfo() }
        .sortedBy { it.size.width }
        .toList(),

    excerpt = deviation.excerpt)

private fun DailyDeviationEntity.toDailyDeviation(giver: UserEntity): Deviation.DailyDeviation {
    if (giverId != giver.id) {
        throw IllegalArgumentException("Expected user with id $giverId but got ${giver.id}")
    }
    return Deviation.DailyDeviation(body = body, date = date, giver = giver.toUser())
}

fun DeviationPropertiesEntity.toDeviationProperties(): Deviation.Properties = Deviation.Properties(
    isDownloadable = isDownloadable,
    isFavorite = isFavorite,
    isMature = isMature,
    allowsComments = allowsComments,
    downloadFileSize = downloadFileSize)

private fun DeviationImageEntity.toImageInfo(): Deviation.ImageInfo = Deviation.ImageInfo(size = size, url = url)

private fun createImageEntity(deviationId: String, type: DeviationImageType, imageDto: ImageDto): DeviationImageEntity {
    val size = Size(imageDto.width, imageDto.height)
    val id = when (type) {
        DeviationImageType.Content, DeviationImageType.Preview -> "$deviationId-${type.id}"
        DeviationImageType.Thumbnail -> "$deviationId-${type.id}-$size"
    }
    return DeviationImageEntity(id = id, deviationId = deviationId, type = type, size = size, url = imageDto.url)
}
