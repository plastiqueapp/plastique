package io.plastique.watch

import io.plastique.core.content.ContentState
import io.plastique.core.lists.ListItem
import io.plastique.core.snackbar.SnackbarState

data class WatcherListViewState(
    val username: String?,

    val contentState: ContentState,
    val items: List<ListItem> = emptyList(),
    val watcherItems: List<ListItem> = emptyList(),
    val signInNeeded: Boolean,
    val snackbarState: SnackbarState = SnackbarState.None,

    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false
) {
    val isPagingEnabled: Boolean
        get() = contentState === ContentState.Content && hasMore && !isLoadingMore && !isRefreshing

    override fun toString(): String {
        return "WatcherListViewState(" +
                "username=$username, " +
                "contentState=$contentState, " +
                "items=${items.size}, " +
                "watcherItems=${watcherItems.size}, " +
                "signInNeeded=$signInNeeded, " +
                "snackbarState=$snackbarState, " +
                "hasMore=$hasMore, " +
                "isLoadingMore=$isLoadingMore, " +
                "isRefreshing=$isRefreshing" +
                ")"
    }
}
