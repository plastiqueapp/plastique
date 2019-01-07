package io.plastique.statuses.list

import io.plastique.core.content.ContentState
import io.plastique.core.lists.ListItem
import io.plastique.core.snackbar.SnackbarState
import io.plastique.statuses.StatusListLoadParams

data class StatusListViewState(
    val params: StatusListLoadParams,

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
                "params=$params, " +
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
