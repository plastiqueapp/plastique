package io.plastique.comments

import io.plastique.api.comments.CommentDto
import io.plastique.api.comments.CommentService
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class CommentSender @Inject constructor(
    private val commentService: CommentService,
    private val commentRepository: CommentRepository
) {
    fun sendComment(threadId: CommentThreadId, text: String, parentCommentId: String?): Completable {
        return postCommentInternal(threadId, text, parentCommentId)
                .doOnSuccess { comment -> commentRepository.put(listOf(comment)) }
                .ignoreElement()
    }

    private fun postCommentInternal(threadId: CommentThreadId, text: String, parentCommentId: String?): Single<CommentDto> = when (threadId) {
        is CommentThreadId.Deviation -> commentService.postCommentOnDeviation(threadId.deviationId, parentCommentId, text)
        is CommentThreadId.Profile -> commentService.postCommentOnProfile(threadId.username, parentCommentId, text)
        is CommentThreadId.Status -> commentService.postCommentOnStatus(threadId.statusId, parentCommentId, text)
    }
}
