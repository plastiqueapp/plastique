package io.plastique.feed

import android.util.SparseArray
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.room.RoomDatabase
import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.collections.FolderDto
import io.plastique.api.deviations.DeviationDto
import io.plastique.api.feed.FeedElementDto
import io.plastique.api.feed.FeedElementTypes
import io.plastique.api.feed.FeedService
import io.plastique.api.statuses.StatusDto
import io.plastique.collections.CollectionFolderRepository
import io.plastique.collections.toFolder
import io.plastique.core.cache.CacheEntry
import io.plastique.core.cache.CacheEntryRepository
import io.plastique.core.cache.CacheHelper
import io.plastique.core.cache.CleanableRepository
import io.plastique.core.cache.MetadataValidatingCacheEntryChecker
import io.plastique.core.converters.NullFallbackConverter
import io.plastique.core.paging.PagedData
import io.plastique.core.paging.StringCursor
import io.plastique.deviations.DeviationRepository
import io.plastique.deviations.toDeviation
import io.plastique.statuses.StatusRepository
import io.plastique.statuses.toStatus
import io.plastique.users.UserRepository
import io.plastique.users.toUser
import io.plastique.util.RxRoom
import io.plastique.util.TimeProvider
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.threeten.bp.Duration
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
) : CleanableRepository {

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
        return RxRoom.createObservable(database, DATA_TABLES) {
            val feedElements = feedDao.getFeed().map { it.toFeedElement() }
            val nextCursor = getNextCursor(cacheKey)
            PagedData(feedElements, nextCursor)
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
                val cacheMetadata = FeedCacheMetadata(matureContent = matureContent, nextCursor = nextCursor)
                val cacheEntry = CacheEntry(key = CACHE_KEY, timestamp = timeProvider.currentInstant, metadata = metadataConverter.toJson(cacheMetadata))
                persist(cacheEntry = cacheEntry, feedElements = feedResult.items, replaceExisting = cursor == null)
                nextCursor.toOptional()
            }
    }

    private fun persist(cacheEntry: CacheEntry, feedElements: List<FeedElementDto>, replaceExisting: Boolean) {
        val users = feedElements.asSequence()
            .filter { it !== FeedElementDto.Unknown }
            .map { feedElement -> feedElement.user }
            .distinctBy { user -> user.id }
            .toList()

        val feedElementEntities = feedElements.asSequence()
            .filter { it !== FeedElementDto.Unknown }
            .map { it.toFeedElementEntity() }
            .toList()

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
            deviationsByIndex.forEach { elementIndex, deviationIds ->
                deviationLinks += deviationIds.asSequence()
                    .mapIndexed { index, deviationId -> FeedElementDeviation(feedElementIds[elementIndex], deviationId, index) }
            }

            feedDao.insertDeviationLinks(deviationLinks)
        }
    }

    override fun cleanCache(): Completable = Completable.fromAction {
        database.runInTransaction {
            cacheEntryRepository.deleteEntryByKey(CACHE_KEY)
            feedDao.deleteAll()
        }
    }

    companion object {
        const val CACHE_KEY = "feed"
        private val CACHE_DURATION = Duration.ofHours(1)
        private val DATA_TABLES = arrayOf(
            "deviations", "collection_folders", "deviation_images", "deviation_videos", "feed_deviations_ordered", "statuses", "feed", "users")
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
                deviations = deviations.map { it.deviationEntityWithRelations.toDeviation() })
        } else {
            FeedElement.DeviationSubmitted(
                timestamp = feedElement.timestamp,
                user = user,
                deviation = deviations.first().deviationEntityWithRelations.toDeviation())
        }

        FeedElementTypes.JOURNAL_SUBMITTED -> FeedElement.JournalSubmitted(
            timestamp = feedElement.timestamp,
            user = user,
            deviation = deviations.first().deviationEntityWithRelations.toDeviation())

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
