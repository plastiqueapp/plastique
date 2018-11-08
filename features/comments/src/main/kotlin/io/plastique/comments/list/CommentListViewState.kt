package io.plastique.comments.list

import io.plastique.comments.CommentThreadId
import io.plastique.core.content.ContentState
import io.plastique.core.lists.ListItem
import io.plastique.core.snackbar.SnackbarState

data class CommentListViewState(
    val threadId: CommentThreadId,

    val title: String = "",
    val contentState: ContentState,
    val comments: List<CommentUiModel> = emptyList(),
    val items: List<ListItem> = emptyList(),
    val commentItems: List<ListItem> = emptyList(),
    val replyComment: CommentUiModel? = null,
    val commentDraft: String = "",
    val snackbarState: SnackbarState = SnackbarState.None,

    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSignedIn: Boolean = false,
    val isPostingComment: Boolean = false
) {
    val isPagingEnabled: Boolean
        get() = contentState === ContentState.Content && hasMore && !isLoadingMore && !isRefreshing

    val showCompose: Boolean
        get() = contentState === ContentState.Content || contentState is ContentState.Empty && !contentState.isError

    override fun toString(): String {
        return "CommentListViewState(" +
                "threadId=$threadId, " +
                "title='$title', " +
                "contentState=$contentState, " +
                "comments=${comments.size}, " +
                "items=${items.size}, " +
                "commentItems=${commentItems.size}, " +
                "replyComment=$replyComment, " +
                "commentDraft='$commentDraft', " +
                "snackbarState=$snackbarState, " +
                "hasMore=$hasMore, " +
                "isLoadingMore=$isLoadingMore, " +
                "isRefreshing=$isRefreshing, " +
                "isSignedIn=$isSignedIn, " +
                "isPostingComment=$isPostingComment" +
                ")"
    }
}
