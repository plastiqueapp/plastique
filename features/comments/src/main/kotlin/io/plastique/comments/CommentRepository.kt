package io.plastique.comments

import androidx.room.RoomDatabase
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.comments.CommentList
import io.plastique.api.comments.CommentService
import io.plastique.api.comments.HideReason
import io.plastique.core.cache.CacheEntry
import io.plastique.core.cache.CacheEntryRepository
import io.plastique.core.cache.CacheHelper
import io.plastique.core.cache.DurationBasedCacheEntryChecker
import io.plastique.core.converters.NullFallbackConverter
import io.plastique.core.paging.OffsetCursor
import io.plastique.core.paging.PagedData
import io.plastique.users.UserRepository
import io.plastique.users.toUserEntity
import io.plastique.util.RxRoom
import io.plastique.util.TimeProvider
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import java.util.concurrent.Callable
import javax.inject.Inject
import io.plastique.api.comments.Comment as CommentDto

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
        val cacheKey = threadId.key
        return cacheHelper.createObservable(
                cacheKey = cacheKey,
                cachedData = getCommentsFromDb(cacheKey),
                updater = fetchComments(threadId))
    }

    fun fetchComments(threadId: CommentThreadId, cursor: OffsetCursor? = null): Completable {
        val offset = cursor?.offset ?: 0
        return getCommentList(threadId, null, COMMENTS_MAX_DEPTH, offset, COMMENTS_PER_PAGE)
                .map { commentList ->
                    val metadata = CommentCacheMetadata(commentList.nextCursor)
                    persistComments(threadId.key, commentList, timeProvider.currentInstant, metadata, offset == 0)
                }
                .ignoreElement()

        // TODO: Ignore duplicates if offset changes
        // TODO: Load nested comments automatically
    }

    private fun getCommentsFromDb(key: String): Observable<PagedData<List<Comment>, OffsetCursor>> {
        return RxRoom.createObservable(database, arrayOf("comments", "comment_linkage")) {
            database.runInTransaction(Callable {
                val commentsWithAuthors = commentDao.getCommentsWithAuthors(key)
                val nextCursor = getNextCursor(key)
                val comments = combineAndFilter(commentsWithAuthors)
                PagedData(comments, nextCursor)
            })
        }.distinctUntilChanged()
    }

    private fun combineAndFilter(commentsWithAuthors: List<CommentWithAuthor>): List<Comment> {
        return commentsWithAuthors.asSequence()
                .filter { commentWithAuthor -> !isIgnoredComment(commentWithAuthor.comment) }
                .map { commentWithAuthor -> commentWithAuthor.toComment() }
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
        is CommentThreadId.Status -> commentService.getCommentsOnStatus(threadId.statusId, parentCommentId, maxDepth, offset, pageSize)
    }

    private fun persistComments(key: String, commentList: CommentList, timestamp: Instant, metadata: CommentCacheMetadata, replaceExisting: Boolean) {
        val users = commentList.comments.asSequence()
                .map { comment -> comment.author.toUserEntity() }
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

    fun persistComment(comment: CommentDto): Comment {
        val commentEntity = comment.toCommentEntity()
        val authorEntity = comment.author.toUserEntity()
        database.runInTransaction {
            userRepository.put(authorEntity)
            commentDao.insertOrUpdate(commentEntity)
        }
        return commentEntity.toComment(authorEntity)
    }

    private fun isIgnoredComment(comment: CommentEntity): Boolean =
            comment.hidden == HideReason.HIDDEN_AS_SPAM && comment.numReplies == 0

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
