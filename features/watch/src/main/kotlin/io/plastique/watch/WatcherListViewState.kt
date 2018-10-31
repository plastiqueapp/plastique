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
    val loadingMore: Boolean = false,
    val refreshing: Boolean = false
) {
    val pagingEnabled: Boolean
        get() = contentState === ContentState.Content && hasMore && !loadingMore && !refreshing

    override fun toString(): String {
        return "WatcherListViewState(" +
                "username=$username, " +
                "contentState=$contentState, " +
                "items=${items.size}, " +
                "watcherItems=${watcherItems.size}, " +
                "signInNeeded=$signInNeeded, " +
                "snackbarState=$snackbarState, " +
                "hasMore=$hasMore, " +
                "loadingMore=$loadingMore, " +
                "refreshing=$refreshing" +
                ")"
    }
}
