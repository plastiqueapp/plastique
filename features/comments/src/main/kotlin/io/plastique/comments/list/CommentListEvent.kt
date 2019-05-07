package io.plastique.comments.list

import com.sch.neon.Event
import io.plastique.core.network.NetworkConnectionState
import io.plastique.core.session.Session

sealed class CommentListEvent : Event() {
    data class CommentsChangedEvent(val comments: List<CommentUiModel>, val hasMore: Boolean) : CommentListEvent() {
        override fun toString(): String =
                "CommentsChangedEvent(items=${comments.size}, hasMore=$hasMore)"
    }

    data class LoadErrorEvent(val error: Throwable) : CommentListEvent()
    object RetryClickEvent : CommentListEvent()

    object LoadMoreEvent : CommentListEvent()
    object LoadMoreFinishedEvent : CommentListEvent()
    data class LoadMoreErrorEvent(val error: Throwable) : CommentListEvent()

    object RefreshEvent : CommentListEvent()
    object RefreshFinishedEvent : CommentListEvent()
    data class RefreshErrorEvent(val error: Throwable) : CommentListEvent()

    data class PostCommentEvent(val text: String) : CommentListEvent()
    object CommentPostedEvent : CommentListEvent()
    data class PostCommentErrorEvent(val error: Throwable) : CommentListEvent()

    data class ReplyClickEvent(val commentId: String) : CommentListEvent()
    object CancelReplyClickEvent : CommentListEvent()

    data class TitleLoadedEvent(val title: String) : CommentListEvent()
    data class ConnectionStateChangedEvent(val connectionState: NetworkConnectionState) : CommentListEvent()
    data class SessionChangedEvent(val session: Session) : CommentListEvent()
    object SnackbarShownEvent : CommentListEvent()
}
