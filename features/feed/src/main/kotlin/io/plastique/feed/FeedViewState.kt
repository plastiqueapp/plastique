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
    val snackbarState: SnackbarState? = null,

    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val isApplyingSettings: Boolean = false,
    val showProgressDialog: Boolean = false
) {
    val isPagingEnabled: Boolean
        get() = contentState === ContentState.Content && hasMore && !isLoadingMore && !isRefreshing

    override fun toString(): String {
        return "FeedViewState(" +
                "contentState=$contentState, " +
                "isSignedIn=$isSignedIn, " +
                "showMatureContent=$showMatureContent, " +
                "items=${items.size}, " +
                "feedItems=${feedItems.size}, " +
                "snackbarState=$snackbarState, " +
                "hasMore=$hasMore, " +
                "isLoadingMore=$isLoadingMore, " +
                "isRefreshing=$isRefreshing, " +
                "isApplyingSettings=$isApplyingSettings, " +
                "showProgressDialog=$showProgressDialog" +
                ")"
    }
}
