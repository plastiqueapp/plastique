package io.plastique.statuses.list

import io.plastique.core.content.ContentState
import io.plastique.core.lists.ListItem
import io.plastique.core.snackbar.SnackbarState

data class StatusListViewState(
    val username: String,

    val contentState: ContentState,
    val items: List<ListItem> = emptyList(),
    val statusItems: List<ListItem> = emptyList(),
    val snackbarState: SnackbarState = SnackbarState.None,

    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false
) {
    val isPagingEnabled: Boolean
        get() = contentState === ContentState.Content && hasMore && !isLoadingMore && !isRefreshing

    override fun toString(): String {
        return "StatusListViewState(" +
                "username='$username', " +
                "contentState=$contentState, " +
                "items=${items.size}, " +
                "statusItems=${statusItems.size}, " +
                "snackbarState=$snackbarState, " +
                "hasMore=$hasMore, " +
                "isLoadingMore=$isLoadingMore, " +
                "isRefreshing=$isRefreshing" +
                ")"
    }
}
