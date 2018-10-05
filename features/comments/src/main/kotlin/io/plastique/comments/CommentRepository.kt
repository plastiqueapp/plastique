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
import io.plastique.users.UserDao
import io.plastique.users.UserMapper
import io.plastique.util.RxRoom
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
    private val commentMapper: CommentMapper,
    private val commentEntityMapper: CommentEntityMapper,
    private val userDao: UserDao,
    private val userMapper: UserMapper
) {
    private val cacheHelper = CacheHelper(cacheEntryRepository, DurationBasedCacheEntryChecker(CACHE_DURATION))

    fun getComments(target: CommentTarget): Observable<PagedData<List<Comment>, OffsetCursor>> {
        val cacheKey = target.key
        return cacheHelper.createObservable(
                cacheKey = cacheKey,
                cachedData = getCommentsFromDb(cacheKey),
                updater = fetchComments(target))
    }

    fun fetchComments(target: CommentTarget, cursor: OffsetCursor? = null): Completable {
        val offset = cursor?.offset ?: 0
        return getCommentList(target, null, COMMENTS_MAX_DEPTH, offset, COMMENTS_PER_PAGE)
                .map { commentList ->
                    val nextCursor = if (commentList.hasMore) OffsetCursor(commentList.nextOffset!!) else null
                    val metadata = CommentCacheMetadata(nextCursor)
                    persistComments(target.key, commentList, Instant.now(), metadata, offset == 0)
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
                .map { commentWithAuthor -> commentEntityMapper.map(commentWithAuthor) }
                .toList()
    }

    private fun getNextCursor(cacheKey: String): OffsetCursor? {
        val cacheEntry = cacheEntryRepository.getEntryByKey(cacheKey)
        val metadata = cacheEntry?.metadata?.let { metadataConverter.fromJson<CommentCacheMetadata>(it) }
        return metadata?.nextCursor
    }

    private fun getCommentList(target: CommentTarget, parentCommentId: String?, maxDepth: Int, offset: Int, pageSize: Int): Single<CommentList> = when (target) {
        is CommentTarget.Deviation -> commentService.getCommentsOnDeviation(target.deviationId, parentCommentId, maxDepth, offset, pageSize)
        is CommentTarget.Profile -> commentService.getCommentsOnProfile(target.username, parentCommentId, maxDepth, offset, pageSize)
        is CommentTarget.Status -> commentService.getCommentsOnStatus(target.statusId, parentCommentId, maxDepth, offset, pageSize)
    }

    private fun persistComments(key: String, commentList: CommentList, timestamp: Instant, metadata: CommentCacheMetadata, replaceExisting: Boolean) {
        val users = commentList.comments.asSequence()
                .map { comment -> userMapper.map(comment.author) }
                .distinctBy { user -> user.id }
                .toList()

        val comments = commentList.comments.map { comment -> commentMapper.map(comment) }
        database.runInTransaction {
            userDao.insertOrUpdate(users)
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
        val entity = commentMapper.map(comment)
        val author = userMapper.map(comment.author)
        database.runInTransaction {
            userDao.insertOrUpdate(author)
            commentDao.insertOrUpdate(entity)
        }
        return commentEntityMapper.map(entity, author)
    }

    private fun isIgnoredComment(comment: CommentEntity): Boolean =
            comment.hidden == HideReason.HIDDEN_AS_SPAM && comment.numReplies == 0

    companion object {
        private const val COMMENTS_PER_PAGE = 50
        private const val COMMENTS_MAX_DEPTH = 5
        private val CACHE_DURATION = Duration.ofHours(1)
    }
}

@JsonClass(generateAdapter = true)
data class CommentCacheMetadata(
    @Json(name = "next_cursor")
    val nextCursor: OffsetCursor? = null
)
