package io.plastique.notifications

import androidx.room.RoomDatabase
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.common.ErrorType
import io.plastique.api.messages.MessageDto
import io.plastique.api.messages.MessageService
import io.plastique.api.messages.MessageTypes
import io.plastique.collections.CollectionFolderRepository
import io.plastique.collections.Folder
import io.plastique.comments.CommentRepository
import io.plastique.core.cache.CacheEntry
import io.plastique.core.cache.CacheEntryRepository
import io.plastique.core.cache.CacheHelper
import io.plastique.core.cache.DurationBasedCacheEntryChecker
import io.plastique.core.converters.NullFallbackConverter
import io.plastique.core.exceptions.ApiException
import io.plastique.core.paging.PagedData
import io.plastique.core.paging.StringCursor
import io.plastique.deviations.DeviationRepository
import io.plastique.deviations.toDeviation
import io.plastique.statuses.StatusRepository
import io.plastique.users.UserRepository
import io.plastique.users.toUser
import io.plastique.util.Optional
import io.plastique.util.RxRoom
import io.plastique.util.TimeProvider
import io.plastique.util.toOptional
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.threeten.bp.Duration
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
    private val metadataConverter: NullFallbackConverter,
    private val timeProvider: TimeProvider
) {

    fun getMessages(): Observable<PagedData<List<Message>, StringCursor>> {
        val cacheHelper = CacheHelper(cacheEntryRepository, DurationBasedCacheEntryChecker(timeProvider, CACHE_DURATION))
        return cacheHelper.createObservable(
                cacheKey = CACHE_KEY,
                cachedData = getMessagesFromDb(CACHE_KEY),
                updater = fetch(null).ignoreElement())
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

    private fun getMessagesFromDb(cacheKey: String): Observable<PagedData<List<Message>, StringCursor>> {
        return RxRoom.createObservable(database, arrayOf("users", "deviation_images", "deviations", "collection_folders", "messages", "deleted_messages")) {
            val messages = messageDao.getMessages()
                    .asSequence()
                    .map { it.toMessage() }
                    .filterNotNull()
                    .toList()
            val nextCursor = getNextCursor(cacheKey)
            PagedData(messages, nextCursor)
        }.distinctUntilChanged()
    }

    private fun getNextCursor(cacheKey: String): StringCursor? {
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

    fun markAsDeleted(messageId: String, deleted: Boolean): Completable {
        return Completable.fromAction {
            if (deleted) {
                messageDao.insertDeletedMessage(DeletedMessageEntity(messageId))
            } else {
                messageDao.clearDeletedMessage(messageId)
            }
        }
    }

    fun deleteMarkedMessages(): Completable {
        return messageDao.getDeletedMessageIds()
                .flattenAsObservable { it }
                .flatMapCompletable { messageId ->
                    messageService.deleteMessage(messageId)
                            .onErrorResumeNext { error ->
                                if (error is ApiException && error.errorData.type === ErrorType.InvalidRequest) {
                                    // Already deleted or messageId is invalid
                                    Completable.complete()
                                } else {
                                    Completable.error(error)
                                }
                            }
                            .doOnComplete {
                                database.runInTransaction {
                                    messageDao.deleteMessageById(messageId)
                                    messageDao.clearDeletedMessage(messageId)
                                }
                            }
                }
    }

    fun clearCache(): Completable = Completable.fromAction {
        database.runInTransaction {
            cacheEntryRepository.deleteEntryByKey(CACHE_KEY)
            messageDao.deleteAllMessages()
            messageDao.clearDeletedMessages()
        }
    }

    companion object {
        private const val CACHE_KEY = "messages"
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
        subject = MessageSubjectEntity(
                deviationId = subject?.deviation?.id,
                commentId = subject?.comment?.id,
                collectionFolderId = subject?.collection?.id))

private fun MessageEntityWithRelations.toMessage(): Message? = when (message.type) {
    MessageTypes.BADGE_GIVEN -> Message.BadgeGiven(
            id = message.id,
            time = message.time,
            user = originator.first().toUser(),
            text = message.html!!)

    MessageTypes.COLLECT -> Message.AddToCollection(
            id = message.id,
            time = message.time,
            user = originator.first().toUser(),
            deviation = subjectDeviation.first().toDeviation(),
            folder = collectionFolder.first().let { Folder(id = it.id, name = it.name, size = it.size, thumbnailUrl = it.thumbnailUrl) })

    MessageTypes.FAVORITE -> Message.Favorite(
            id = message.id,
            time = message.time,
            user = originator.first().toUser(),
            deviation = subjectDeviation.first().toDeviation())

    MessageTypes.WATCH -> Message.Watch(
            id = message.id,
            time = message.time,
            user = originator.first().toUser())

    else -> null // Unknown type
}
