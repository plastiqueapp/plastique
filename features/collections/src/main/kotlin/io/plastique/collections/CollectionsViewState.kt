package io.plastique.collections

import io.plastique.core.content.ContentState
import io.plastique.core.lists.ListItem
import io.plastique.core.snackbar.SnackbarState

data class CollectionsViewState(
    val params: FolderLoadParams,

    val contentState: ContentState,
    val signInNeeded: Boolean,

    val collectionItems: List<ListItem> = emptyList(),
    val items: List<ListItem> = emptyList(),
    val snackbarState: SnackbarState = SnackbarState.None,

    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false
) {
    val isPagingEnabled: Boolean
        get() = contentState === ContentState.Content && hasMore && !isLoadingMore && !isRefreshing

    val showMenu: Boolean
        get() = params.username == null && contentState == ContentState.Content

    override fun toString(): String {
        return "CollectionsViewState(" +
                "params=$params, " +
                "contentState=$contentState, " +
                "signInNeeded=$signInNeeded, " +
                "collectionItems=${collectionItems.size}, " +
                "items=${items.size}, " +
                "snackbarState=$snackbarState, " +
                "hasMore=$hasMore, " +
                "isLoadingMore=$isLoadingMore, " +
                "isRefreshing=$isRefreshing" +
                ")"
    }
}
