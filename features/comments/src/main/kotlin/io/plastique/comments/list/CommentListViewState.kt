package io.plastique.comments.list

import io.plastique.comments.CommentThreadId
import io.plastique.core.content.ContentState
import io.plastique.core.lists.PagedListState
import io.plastique.core.snackbar.SnackbarState

data class CommentListViewState(
    val threadId: CommentThreadId,

    val title: String = "",
    val contentState: ContentState,
    val comments: List<CommentUiModel> = emptyList(),
    val replyComment: CommentUiModel? = null,
    val commentDraft: String = "",
    val listState: PagedListState = PagedListState.Empty,
    val snackbarState: SnackbarState? = null,
    val isSignedIn: Boolean = false,
    val isPostingComment: Boolean = false
) {
    val showCompose: Boolean
        get() = contentState === ContentState.Content || contentState is ContentState.Empty && !contentState.isError

    override fun toString(): String {
        return "CommentListViewState(" +
                "threadId=$threadId, " +
                "title=$title, " +
                "contentState=$contentState, " +
                "comments=${comments.size}, " +
                "replyComment=$replyComment, " +
                "commentDraft=$commentDraft, " +
                "listState=$listState, " +
                "snackbarState=$snackbarState, " +
                "isSignedIn=$isSignedIn, " +
                "isPostingComment=$isPostingComment" +
                ")"
    }
}
