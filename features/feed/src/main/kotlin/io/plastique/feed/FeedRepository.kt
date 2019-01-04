package io.plastique.feed

import android.util.SparseArray
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.room.RoomDatabase
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.collections.FolderDto
import io.plastique.api.deviations.DeviationDto
import io.plastique.api.feed.FeedElementDto
import io.plastique.api.feed.FeedElementTypes
import io.plastique.api.feed.FeedService
import io.plastique.api.users.StatusDto
import io.plastique.collections.CollectionFolderRepository
import io.plastique.collections.Folder
import io.plastique.collections.FolderEntity
import io.plastique.core.cache.CacheEntry
import io.plastique.core.cache.CacheEntryRepository
import io.plastique.core.cache.CacheHelper
import io.plastique.core.cache.MetadataValidatingCacheEntryChecker
import io.plastique.core.converters.NullFallbackConverter
import io.plastique.core.paging.PagedData
import io.plastique.core.paging.StringCursor
import io.plastique.deviations.DailyDeviationEntity
import io.plastique.deviations.Deviation
import io.plastique.deviations.DeviationImageEntity
import io.plastique.deviations.DeviationImageType
import io.plastique.deviations.DeviationRepository
import io.plastique.deviations.toDeviationProperties
import io.plastique.statuses.StatusRepository
import io.plastique.statuses.toStatus
import io.plastique.users.UserEntity
import io.plastique.users.UserRepository
import io.plastique.users.toUser
import io.plastique.util.Optional
import io.plastique.util.RxRoom
import io.plastique.util.TimeProvider
import io.plastique.util.toOptional
import io.reactivex.Observable
import io.reactivex.Single
import org.threeten.bp.Duration
import org.threeten.bp.ZoneId
import java.util.concurrent.Callable
import javax.inject.Inject
import kotlin.math.max

class FeedRepository @Inject constructor(
    private val database: RoomDatabase,
    private val feedDao: FeedDao,
    private val feedService: FeedService,
    private val cacheEntryRepository: CacheEntryRepository,
    private val deviationRepository: DeviationRepository,
    private val collectionFolderRepository: CollectionFolderRepository,
    private val statusRepository: StatusRepository,
    private val userRepository: UserRepository,
    private val metadataConverter: NullFallbackConverter,
    private val timeProvider: TimeProvider
) {
    fun getFeed(matureContent: Boolean): Observable<PagedData<List<FeedElement>, StringCursor>> {
        val cacheEntryChecker = MetadataValidatingCacheEntryChecker(timeProvider, CACHE_DURATION) { serializedMetadata ->
            val cacheMetadata = metadataConverter.fromJson<FeedCacheMetadata>(serializedMetadata)
            cacheMetadata?.matureContent == matureContent
        }
        val cacheHelper = CacheHelper(cacheEntryRepository, cacheEntryChecker)
        return cacheHelper.createObservable(
                cacheKey = CACHE_KEY,
                cachedData = getFromDb(CACHE_KEY),
                updater = fetch(matureContent, null).ignoreElement())
    }

    private fun getFromDb(cacheKey: String): Observable<PagedData<List<FeedElement>, StringCursor>> {
        return RxRoom.createObservable(database, arrayOf("users", "collection_folders", "deviation_images", "feed_deviations_ordered", "deviations", "statuses", "feed")) {
            database.runInTransaction(Callable {
                val feedElements = feedDao.getFeed().map { it.toFeedElement() }
                val nextCursor = getNextCursor(cacheKey)
                PagedData(feedElements, nextCursor)
            })
        }
    }

    private fun getNextCursor(cacheKey: String): StringCursor? {
        val cacheEntry = cacheEntryRepository.getEntryByKey(cacheKey)
        val cacheMetadata = cacheEntry?.metadata?.let { metadataConverter.fromJson<FeedCacheMetadata>(it) }
        return cacheMetadata?.nextCursor
    }

    fun fetch(matureContent: Boolean, cursor: StringCursor?): Single<Optional<StringCursor>> {
        return feedService.getHomeFeed(cursor?.value, matureContent)
                .map { feedResult ->
                    val nextCursor = if (feedResult.hasMore) StringCursor(feedResult.cursor!!) else null
                    persist(feedElements = feedResult.items, matureContent = matureContent, replaceExisting = cursor == null, nextCursor = nextCursor)
                    nextCursor.toOptional()
                }
    }

    private fun persist(feedElements: List<FeedElementDto>, matureContent: Boolean, replaceExisting: Boolean, nextCursor: StringCursor?) {
        val users = feedElements.asSequence()
                .filter { it !== FeedElementDto.Unknown }
                .map { feedElement -> feedElement.user }
                .distinctBy { user -> user.id }
                .toList()

        val feedElementEntities = feedElements.asSequence()
                .filter { it !== FeedElementDto.Unknown }
                .map { it.toFeedElementEntity() }
                .toList()

        val cacheMetadata = FeedCacheMetadata(matureContent, nextCursor)
        val cacheEntry = CacheEntry(CACHE_KEY, timeProvider.currentInstant, metadataConverter.toJson(cacheMetadata))

        val deviations = mutableListOf<DeviationDto>()
        val statuses = mutableListOf<StatusDto>()
        val deviationsByIndex = SparseArray<List<String>>()
        val collectionFolders = mutableListOf<FolderDto>()

        feedElements.forEachIndexed { index, feedElement ->
            when (feedElement) {
                is FeedElementDto.CollectionUpdate -> {
                    collectionFolders += feedElement.folder
                }

                is FeedElementDto.DeviationSubmitted -> {
                    deviations += feedElement.deviations
                    deviationsByIndex[index] = feedElement.deviations.map { it.id }
                }

                is FeedElementDto.JournalSubmitted -> {
                    deviations += feedElement.deviations
                    deviationsByIndex[index] = feedElement.deviations.map { it.id }
                }

                is FeedElementDto.StatusUpdate -> {
                    statuses += feedElement.status
                }
            }
        }

        database.runInTransaction {
            if (replaceExisting) {
                feedDao.deleteAll()
            }

            cacheEntryRepository.setEntry(cacheEntry)
            userRepository.put(users)
            statusRepository.put(statuses)
            deviationRepository.put(deviations)
            collectionFolderRepository.put(collectionFolders)

            val feedElementIds = feedDao.insert(feedElementEntities)

            val deviationLinks = mutableListOf<FeedElementDeviation>()
            deviationsByIndex.forEach { index, deviationIds ->
                var order = 0
                deviationLinks += deviationIds.asSequence()
                        .map { deviationId -> FeedElementDeviation(feedElementIds[index], deviationId, order++) }
            }

            feedDao.insertDeviationLinks(deviationLinks)
        }
    }

    companion object {
        const val CACHE_KEY = "feed"
        private val CACHE_DURATION = Duration.ofHours(1)
    }
}

@JsonClass(generateAdapter = true)
data class FeedCacheMetadata(
    @Json(name = "mature_content")
    val matureContent: Boolean,

    @Json(name = "next_cursor")
    val nextCursor: StringCursor?
)

private fun FeedElementEntityWithRelations.toFeedElement(): FeedElement {
    val user = users.first().toUser()
    return when (feedElement.type) {
        FeedElementTypes.COLLECTION_UPDATE -> FeedElement.CollectionUpdate(
                timestamp = feedElement.timestamp,
                user = user,
                folder = collectionFolders.first().toFolder(),
                addedCount = feedElement.addedCount)

        FeedElementTypes.DEVIATION_SUBMITTED -> if (deviations.size > 1) {
            FeedElement.MultipleDeviationsSubmitted(
                    timestamp = feedElement.timestamp,
                    user = user,
                    submittedTotal = max(deviations.size, feedElement.bucketTotal),
                    deviations = deviations.map { it.toDeviation() })
        } else {
            FeedElement.DeviationSubmitted(
                    timestamp = feedElement.timestamp,
                    user = user,
                    deviation = deviations.first().toDeviation())
        }

        FeedElementTypes.JOURNAL_SUBMITTED -> FeedElement.JournalSubmitted(
                timestamp = feedElement.timestamp,
                user = user,
                deviation = deviations.first().toDeviation())

        FeedElementTypes.STATUS_UPDATE -> FeedElement.StatusUpdate(
                timestamp = feedElement.timestamp,
                user = user,
                status = statuses.first().toStatus())

        FeedElementTypes.USERNAME_CHANGE -> FeedElement.UsernameChange(
                timestamp = feedElement.timestamp,
                user = user,
                formerName = feedElement.formerName!!)

        else -> throw IllegalArgumentException("Unhandled feed element type ${feedElement.type}")
    }
}

// TODO: Reduce duplication
private fun FeedDeviationEntityWithRelations.toDeviation(): Deviation = Deviation(
        id = deviation.id,
        title = deviation.title,
        url = deviation.url,
        categoryPath = deviation.categoryPath,
        publishTime = deviation.publishTime.atZone(ZoneId.systemDefault()),
        content = images.asSequence()
                .filter { it.type == DeviationImageType.Content }
                .map { it.toImage() }
                .firstOrNull(),
        preview = images.asSequence()
                .filter { it.type == DeviationImageType.Preview }
                .map { it.toImage() }
                .firstOrNull(),
        thumbnails = images.asSequence()
                .filter { it.type == DeviationImageType.Thumbnail }
                .map { it.toImage() }
                .sortedBy { it.size.width }
                .toList(),
        excerpt = deviation.excerpt,
        author = author.first().toUser(),
        properties = deviation.properties.toDeviationProperties(),
        stats = Deviation.Stats(comments = deviation.stats.comments, favorites = deviation.stats.favorites),
        dailyDeviation = deviation.dailyDeviation?.toDailyDeviation(dailyDeviationGiver.first()))

private fun DailyDeviationEntity.toDailyDeviation(giver: UserEntity): Deviation.DailyDeviation {
    if (giverId != giver.id) {
        throw IllegalArgumentException("Expected user with id $giverId but got ${giver.id}")
    }
    return Deviation.DailyDeviation(body = body, date = date, giver = giver.toUser())
}

private fun FeedElementDto.toFeedElementEntity(): FeedElementEntity = when (this) {
    is FeedElementDto.CollectionUpdate -> FeedElementEntity(
            timestamp = timestamp,
            userId = user.id,
            type = FeedElementTypes.COLLECTION_UPDATE,
            folderId = folder.id,
            addedCount = addedCount)

    is FeedElementDto.DeviationSubmitted -> FeedElementEntity(
            timestamp = timestamp,
            userId = user.id,
            type = FeedElementTypes.DEVIATION_SUBMITTED,
            bucketId = bucketId,
            bucketTotal = bucketTotal)

    is FeedElementDto.JournalSubmitted -> FeedElementEntity(
            timestamp = timestamp,
            userId = user.id,
            type = FeedElementTypes.JOURNAL_SUBMITTED)

    is FeedElementDto.StatusUpdate -> FeedElementEntity(
            timestamp = timestamp,
            userId = user.id,
            type = FeedElementTypes.STATUS_UPDATE,
            statusId = status.id)

    is FeedElementDto.UsernameChange -> FeedElementEntity(
            timestamp = timestamp,
            userId = user.id,
            type = FeedElementTypes.USERNAME_CHANGE,
            formerName = formerName)

    FeedElementDto.Unknown -> throw IllegalArgumentException("Unknown feed element type")
}

private fun DeviationImageEntity.toImage(): Deviation.Image = Deviation.Image(size = size, url = url)

private fun FolderEntity.toFolder(): Folder =
        Folder(id = id, name = name, size = size, thumbnailUrl = thumbnailUrl)
