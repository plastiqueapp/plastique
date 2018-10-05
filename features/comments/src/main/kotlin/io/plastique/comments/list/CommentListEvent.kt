package io.plastique.comments.list

import io.plastique.core.content.EmptyState
import io.plastique.core.flow.Event
import io.plastique.core.session.Session

sealed class CommentListEvent : Event() {
    data class CommentsChangedEvent(
        val comments: List<CommentUiModel>,
        val hasMore: Boolean
    ) : CommentListEvent() {
        override fun toString(): String {
            return "CommentsChangedEvent(items=${comments.size}, hasMore=$hasMore)"
        }
    }

    data class LoadErrorEvent(val emptyState: EmptyState) : CommentListEvent()
    object RetryClickEvent : CommentListEvent()

    object LoadMoreEvent : CommentListEvent()
    object LoadMoreFinishedEvent : CommentListEvent()
    data class LoadMoreErrorEvent(val errorMessage: String) : CommentListEvent()

    object RefreshEvent : CommentListEvent()
    object RefreshFinishedEvent : CommentListEvent()
    data class RefreshErrorEvent(val errorMessage: String) : CommentListEvent()

    data class PostCommentEvent(val text: String) : CommentListEvent()
    object CommentPostedEvent : CommentListEvent()
    data class PostCommentErrorEvent(val errorMessage: String) : CommentListEvent()

    data class ReplyClickEvent(val commentId: String) : CommentListEvent()
    object CancelReplyClickEvent : CommentListEvent()

    data class TitleLoadedEvent(val title: String) : CommentListEvent()
    data class SessionChangedEvent(val session: Session) : CommentListEvent()
    object SnackbarShownEvent : CommentListEvent()
}
