package io.plastique.comments.list

import com.sch.neon.Effect
import io.plastique.comments.CommentThreadId

sealed class CommentListEffect : Effect() {
    data class LoadCommentsEffect(val threadId: CommentThreadId) : CommentListEffect()
    data class LoadTitleEffect(val threadId: CommentThreadId) : CommentListEffect()
    object LoadMoreEffect : CommentListEffect()
    object RefreshEffect : CommentListEffect()

    data class PostCommentEffect(
        val threadId: CommentThreadId,
        val text: String,
        val parentCommentId: String?
    ) : CommentListEffect()
}
