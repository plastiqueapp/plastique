package io.plastique.comments.list

import io.plastique.comments.CommentTarget
import io.plastique.core.flow.Effect

sealed class CommentListEffect : Effect() {
    data class LoadCommentsEffect(val target: CommentTarget) : CommentListEffect()
    data class LoadTitleEffect(val target: CommentTarget) : CommentListEffect()
    object LoadMoreEffect : CommentListEffect()
    object RefreshEffect : CommentListEffect()

    data class PostCommentEffect(
        val target: CommentTarget,
        val text: String,
        val parentCommentId: String?
    ) : CommentListEffect()
}
