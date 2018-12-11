package io.plastique.feed

import io.plastique.core.content.ContentState
import io.plastique.core.lists.ListItem
import io.plastique.core.snackbar.SnackbarState

data class FeedViewState(
    val contentState: ContentState,
    val isSignedIn: Boolean,
    val showMatureContent: Boolean,

    val items: List<ListItem> = emptyList(),
    val feedItems: List<ListItem> = emptyList(),
    val snackbarState: SnackbarState = SnackbarState.None,

    val hasMore: Boolean = false,
    val loadingMore: Boolean = false,
    val refreshing: Boolean = false,
    val applyingSettings: Boolean = false,
    val showProgressDialog: Boolean = false
) {
    val pagingEnabled: Boolean
        get() = contentState === ContentState.Content && hasMore && !loadingMore && !refreshing

    override fun toString(): String {
        return "FeedViewState(" +
                "contentState=$contentState, " +
                "isSignedIn=$isSignedIn, " +
                "showMatureContent=$showMatureContent, " +
                "items=${items.size}, " +
                "feedItems=${feedItems.size}, " +
                "snackbarState=$snackbarState, " +
                "hasMore=$hasMore, " +
                "loadingMore=$loadingMore, " +
                "refreshing=$refreshing, " +
                "applyingSettings=$applyingSettings, " +
                "showProgressDialog=$showProgressDialog" +
                ")"
    }
}
