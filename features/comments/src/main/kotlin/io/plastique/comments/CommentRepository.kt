package io.plastique.comments

import androidx.room.RoomDatabase
import com.sch.rxjava2.extensions.mapError
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.comments.CommentDto
import io.plastique.api.comments.CommentList
import io.plastique.api.comments.CommentService
import io.plastique.api.comments.HideReason
import io.plastique.api.common.ErrorType
import io.plastique.core.cache.CacheEntry
import io.plastique.core.cache.CacheEntryRepository
import io.plastique.core.cache.CacheHelper
import io.plastique.core.cache.DurationBasedCacheEntryChecker
import io.plastique.core.converters.NullFallbackConverter
import io.plastique.core.exceptions.ApiException
import io.plastique.core.exceptions.UserNotFoundException
import io.plastique.core.paging.OffsetCursor
import io.plastique.core.paging.PagedData
import io.plastique.users.UserRepository
import io.plastique.util.Optional
import io.plastique.util.RxRoom
import io.plastique.util.TimeProvider
import io.plastique.util.toOptional
import io.reactivex.Observable
import io.reactivex.Single
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import java.util.concurrent.Callable
import javax.inject.Inject

class CommentRepository @Inject constructor(
    private val database: RoomDatabase,
    private val commentDao: CommentDao,
    private val commentService: CommentService,
    private val cacheEntryRepository: CacheEntryRepository,
    private val metadataConverter: NullFallbackConverter,
    private val timeProvider: TimeProvider,
    private val userRepository: UserRepository
) {
    private val cacheHelper = CacheHelper(cacheEntryRepository, DurationBasedCacheEntryChecker(timeProvider, CACHE_DURATION))

    fun getComments(threadId: CommentThreadId): Observable<PagedData<List<Comment>, OffsetCursor>> {
        val cacheKey = threadId.cacheKey
        return cacheHelper.createObservable(
                cacheKey = cacheKey,
                cachedData = getCommentsFromDb(cacheKey),
                updater = fetchComments(threadId).ignoreElement())
    }

    fun fetchComments(threadId: CommentThreadId, cursor: OffsetCursor? = null): Single<Optional<OffsetCursor>> {
        val offset = cursor?.offset ?: 0
        return getCommentList(threadId, null, COMMENTS_MAX_DEPTH, offset, COMMENTS_PER_PAGE)
                .map { commentList ->
                    val cacheMetadata = CommentCacheMetadata(nextCursor = commentList.nextCursor)
                    persist(threadId.cacheKey, commentList, timeProvider.currentInstant, cacheMetadata, offset == 0)
                    cacheMetadata.nextCursor.toOptional()
                }

        // TODO: Ignore duplicates if offset changes
        // TODO: Load nested comments automatically
    }

    private fun getCommentsFromDb(key: String): Observable<PagedData<List<Comment>, OffsetCursor>> {
        return RxRoom.createObservable(database, arrayOf("users", "comments", "comment_linkage")) {
            database.runInTransaction(Callable {
                val commentsWithRelations = commentDao.getCommentsByKey(key)
                val comments = combineAndFilter(commentsWithRelations)
                val nextCursor = getNextCursor(key)
                PagedData(comments, nextCursor)
            })
        }.distinctUntilChanged()
    }

    private fun combineAndFilter(commentsWithRelations: List<CommentEntityWithRelations>): List<Comment> {
        return commentsWithRelations.asSequence()
                .filter { !it.comment.isIgnored }
                .map { it.toComment() }
                .toList()
    }

    private fun getNextCursor(cacheKey: String): OffsetCursor? {
        val cacheEntry = cacheEntryRepository.getEntryByKey(cacheKey)
        val metadata = cacheEntry?.metadata?.let { metadataConverter.fromJson<CommentCacheMetadata>(it) }
        return metadata?.nextCursor
    }

    private fun getCommentList(threadId: CommentThreadId, parentCommentId: String?, maxDepth: Int, offset: Int, pageSize: Int): Single<CommentList> = when (threadId) {
        is CommentThreadId.Deviation -> commentService.getCommentsOnDeviation(threadId.deviationId, parentCommentId, maxDepth, offset, pageSize)
        is CommentThreadId.Profile -> commentService.getCommentsOnProfile(threadId.username, parentCommentId, maxDepth, offset, pageSize)
                .mapError { error ->
                    if (error is ApiException && error.errorData.type == ErrorType.InvalidRequest) {
                        UserNotFoundException(threadId.username, error)
                    } else {
                        error
                    }
                }
        is CommentThreadId.Status -> commentService.getCommentsOnStatus(threadId.statusId, parentCommentId, maxDepth, offset, pageSize)
    }

    private fun persist(key: String, commentList: CommentList, timestamp: Instant, metadata: CommentCacheMetadata, replaceExisting: Boolean) {
        val users = commentList.comments.asSequence()
                .map { comment -> comment.author }
                .distinctBy { user -> user.id }
                .toList()

        val comments = commentList.comments.map { comment -> comment.toCommentEntity() }
        database.runInTransaction {
            userRepository.put(users)
            commentDao.insertOrUpdate(comments)
            cacheEntryRepository.setEntry(CacheEntry(key, timestamp, metadataConverter.toJson(metadata)))

            var order = if (replaceExisting) {
                commentDao.deleteLinks(key)
                1
            } else {
                commentDao.maxOrder(key) + 1
            }

            val links = comments.map { comment -> CommentLinkage(key, comment.id, order++) }
            commentDao.insertLinks(links)
        }
    }

    fun persistComment(comment: CommentDto) {
        val commentEntity = comment.toCommentEntity()
        database.runInTransaction {
            userRepository.put(listOf(comment.author))
            commentDao.insertOrUpdate(commentEntity)
        }
    }

    private val CommentEntity.isIgnored: Boolean
        get() = hidden == HideReason.HIDDEN_AS_SPAM && numReplies == 0

    companion object {
        private val CACHE_DURATION = Duration.ofHours(1)
        private const val COMMENTS_PER_PAGE = 50
        private const val COMMENTS_MAX_DEPTH = 5
    }
}

@JsonClass(generateAdapter = true)
data class CommentCacheMetadata(
    @Json(name = "next_cursor")
    val nextCursor: OffsetCursor? = null
)

private val CommentList.nextCursor: OffsetCursor?
    get() = if (hasMore) OffsetCursor(nextOffset!!) else null

private val CommentThreadId.cacheKey: String
    get() = when (this) {
        is CommentThreadId.Deviation -> "comments-deviation-$deviationId"
        is CommentThreadId.Profile -> "comments-profile-$username"
        is CommentThreadId.Status -> "comments-status-$statusId"
    }
