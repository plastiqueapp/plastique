package io.plastique.notifications

import androidx.room.RoomDatabase
import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.ApiException
import io.plastique.api.messages.MessageDto
import io.plastique.api.messages.MessageService
import io.plastique.api.messages.MessageTypes
import io.plastique.collections.folders.CollectionFolderRepository
import io.plastique.collections.folders.toFolder
import io.plastique.comments.CommentRepository
import io.plastique.core.cache.CacheEntry
import io.plastique.core.cache.CacheEntryRepository
import io.plastique.core.cache.CacheHelper
import io.plastique.core.cache.CacheKey
import io.plastique.core.cache.CleanableRepository
import io.plastique.core.cache.DurationBasedCacheEntryChecker
import io.plastique.core.cache.toCacheKey
import io.plastique.core.db.createObservable
import io.plastique.core.json.adapters.NullFallbackAdapter
import io.plastique.core.paging.PagedData
import io.plastique.core.paging.StringCursor
import io.plastique.core.time.TimeProvider
import io.plastique.deviations.DeviationRepository
import io.plastique.deviations.toDeviation
import io.plastique.statuses.StatusRepository
import io.plastique.users.UserRepository
import io.plastique.users.toUser
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.internal.functions.Functions
import org.threeten.bp.Duration
import org.threeten.bp.ZoneId
import timber.log.Timber
import javax.inject.Inject

class MessageRepository @Inject constructor(
    private val database: RoomDatabase,
    private val messageDao: MessageDao,
    private val messageService: MessageService,
    private val cacheEntryRepository: CacheEntryRepository,
    private val deviationRepository: DeviationRepository,
    private val collectionFolderRepository: CollectionFolderRepository,
    private val commentRepository: CommentRepository,
    private val statusRepository: StatusRepository,
    private val userRepository: UserRepository,
    private val metadataConverter: NullFallbackAdapter,
    private val timeProvider: TimeProvider
) : CleanableRepository {

    fun getMessages(): Observable<PagedData<List<Message>, StringCursor>> {
        val cacheHelper = CacheHelper(cacheEntryRepository, DurationBasedCacheEntryChecker(timeProvider, CACHE_DURATION))
        return cacheHelper.createObservable(
            cacheKey = CACHE_KEY,
            cachedData = getMessagesFromDb(CACHE_KEY),
            updater = fetch(cursor = null).ignoreElement())
    }

    fun fetch(cursor: StringCursor?): Single<Optional<StringCursor>> {
        return messageService.getAllMessages(cursor?.value)
            .map { result ->
                val nextCursor = if (result.hasMore) StringCursor(result.cursor) else null
                val cacheMetadata = MessageFeedCacheMetadata(nextCursor = nextCursor)
                val cacheEntry = CacheEntry(key = CACHE_KEY, timestamp = timeProvider.currentInstant, metadata = metadataConverter.toJson(cacheMetadata))
                persist(cacheEntry = cacheEntry, messages = result.results, replaceExisting = cursor == null)
                nextCursor.toOptional()
            }
    }

    private fun getMessagesFromDb(cacheKey: CacheKey): Observable<PagedData<List<Message>, StringCursor>> {
        return database.createObservable("users", "deviation_images", "deviations", "collection_folders", "messages", "deleted_messages") {
            val messages = messageDao.getMessages()
                .asSequence()
                .map { it.toMessage(timeProvider.timeZone) }
                .filterNotNull()
                .toList()
            val nextCursor = getNextCursor(cacheKey)
            PagedData(messages, nextCursor)
        }.distinctUntilChanged()
    }

    private fun getNextCursor(cacheKey: CacheKey): StringCursor? {
        val cacheEntry = cacheEntryRepository.getEntryByKey(cacheKey)
        val cacheMetadata = cacheEntry?.metadata?.let { metadataConverter.fromJson<MessageFeedCacheMetadata>(it) }
        return cacheMetadata?.nextCursor
    }

    private fun persist(cacheEntry: CacheEntry, messages: List<MessageDto>, replaceExisting: Boolean) {
        val entities = messages.asSequence()
            .filter { !it.isOrphaned }
            .map { it.toMessageEntity() }
            .toList()

        val users = messages.asSequence()
            .flatMap { sequenceOf(it.originator, it.profile, it.subject?.profile) }
            .filterNotNull()
            .distinctBy { it.id }
            .toList()

        val deviations = messages.asSequence()
            .flatMap { sequenceOf(it.deviation, it.subject?.deviation) }
            .filterNotNull()
            .distinctBy { it.id }
            .toList()

        val comments = messages.asSequence()
            .flatMap { sequenceOf(it.comment, it.subject?.comment) }
            .filterNotNull()
            .map { it.copy(parentId = null) }
            .toList()

        val statuses = messages.asSequence()
            .flatMap { sequenceOf(it.status, it.subject?.status) }
            .filterNotNull()
            .distinctBy { it.id }
            .toList()

        val collectionFolders = messages.asSequence()
            .flatMap { sequenceOf(it.collection, it.subject?.collection) }
            .filterNotNull()
            .distinctBy { it.id }
            .toList()

        database.runInTransaction {
            if (replaceExisting) {
                messageDao.deleteAllMessages()
            }

            cacheEntryRepository.setEntry(cacheEntry)
            userRepository.put(users)
            deviationRepository.put(deviations)
            commentRepository.put(comments)
            statusRepository.put(statuses)
            collectionFolderRepository.put(collectionFolders)
            messageDao.insertOrUpdate(entities)
        }
    }

    fun markAsDeleted(messageId: String, deleted: Boolean): Completable = Completable.fromAction {
        if (deleted) {
            messageDao.insertDeletedMessage(DeletedMessageEntity(messageId))
        } else {
            messageDao.removeDeletedMessage(messageId)
        }
    }

    fun deleteMarkedMessages(): Completable {
        return messageDao.getDeletedMessageIds()
            .flattenAsObservable(Functions.identity())
            .concatMapCompletable { messageId ->
                messageService.deleteMessage(messageId)
                    .toSingleDefault(true)
                    .onErrorResumeNext { error ->
                        if (error is ApiException) {
                            Timber.e(error)
                            Single.just(false)
                        } else {
                            Single.error(error)
                        }
                    }
                    .doOnSuccess { wasDeleted ->
                        database.runInTransaction {
                            if (wasDeleted) {
                                messageDao.deleteMessageById(messageId)
                            }
                            messageDao.removeDeletedMessage(messageId)
                        }
                    }
                    .ignoreElement()
            }
    }

    override fun cleanCache(): Completable = Completable.fromAction {
        database.runInTransaction {
            cacheEntryRepository.deleteEntryByKey(CACHE_KEY)
            messageDao.deleteAllMessages()
            messageDao.removeDeletedMessages()
        }
    }

    companion object {
        private val CACHE_KEY = "messages".toCacheKey()
        private val CACHE_DURATION = Duration.ofHours(1)
    }
}

@JsonClass(generateAdapter = true)
data class MessageFeedCacheMetadata(
    @Json(name = "next_cursor")
    val nextCursor: StringCursor?
)

private fun MessageDto.toMessageEntity(): MessageEntity = MessageEntity(
    id = id,
    type = type,
    time = timestamp!!,
    html = html,
    originatorId = originator!!.id,
    deviationId = deviation?.id,
    commentId = comment?.id,
    collectionFolderId = collection?.id,
    subject = MessageEntity.Subject(
        deviationId = subject?.deviation?.id,
        commentId = subject?.comment?.id,
        collectionFolderId = subject?.collection?.id))

private fun MessageEntityWithRelations.toMessage(timeZone: ZoneId): Message? {
    val data = when (message.type) {
        MessageTypes.BADGE_GIVEN ->
            Message.Data.BadgeGiven(
                text = message.html!!)

        MessageTypes.COLLECT -> {
            val deviation = subjectDeviation!!.toDeviation(timeZone)
            Message.Data.AddToCollection(
                deviationId = deviation.id,
                deviationTitle = deviation.title,
                folder = collectionFolder!!.toFolder())
        }

        MessageTypes.FAVORITE -> {
            val deviation = subjectDeviation!!.toDeviation(timeZone)
            Message.Data.Favorite(
                deviationId = deviation.id,
                deviationTitle = deviation.title)
        }

        MessageTypes.WATCH -> Message.Data.Watch

        else -> return null // Unsupported type
    }
    return Message(
        id = message.id,
        time = message.time,
        user = originator.toUser(),
        data = data)
}
