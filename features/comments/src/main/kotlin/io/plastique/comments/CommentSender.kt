package io.plastique.comments

import io.plastique.api.comments.CommentService
import io.reactivex.Single
import javax.inject.Inject

class CommentSender @Inject constructor(
    private val commentService: CommentService,
    private val commentRepository: CommentRepository
) {
    fun sendComment(threadId: CommentThreadId, text: String, parentCommentId: String?): Single<Comment> {
        return postCommentInternal(threadId, text, parentCommentId)
                .map { comment -> commentRepository.persistComment(comment) }
    }

    private fun postCommentInternal(threadId: CommentThreadId, text: String, parentCommentId: String?) = when (threadId) {
        is CommentThreadId.Deviation -> commentService.postCommentOnDeviation(threadId.deviationId, parentCommentId, text)
        is CommentThreadId.Profile -> commentService.postCommentOnProfile(threadId.username, parentCommentId, text)
        is CommentThreadId.Status -> commentService.postCommentOnStatus(threadId.statusId, parentCommentId, text)
    }
}
