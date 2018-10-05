package io.plastique.comments

import io.plastique.api.comments.CommentService
import io.reactivex.Single
import javax.inject.Inject

class CommentSender @Inject constructor(
    private val commentService: CommentService,
    private val commentRepository: CommentRepository
) {
    fun sendComment(target: CommentTarget, text: String, parentCommentId: String?): Single<Comment> {
        return postCommentInternal(target, text, parentCommentId)
                .map { comment -> commentRepository.persistComment(comment) }
    }

    private fun postCommentInternal(target: CommentTarget, text: String, parentCommentId: String?) = when (target) {
        is CommentTarget.Deviation -> commentService.postCommentOnDeviation(target.deviationId, parentCommentId, text)
        is CommentTarget.Profile -> commentService.postCommentOnProfile(target.username, parentCommentId, text)
        is CommentTarget.Status -> commentService.postCommentOnStatus(target.statusId, parentCommentId, text)
    }
}
