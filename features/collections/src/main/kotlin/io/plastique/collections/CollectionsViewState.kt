package io.plastique.collections

import io.plastique.core.content.ContentState
import io.plastique.core.lists.ListItem

data class CollectionsViewState(
    val params: FolderLoadParams,

    val contentState: ContentState,
    val signInNeeded: Boolean,

    val collectionItems: List<ListItem> = emptyList(),
    val items: List<ListItem> = emptyList(),
    val snackbarMessage: String? = null,

    val hasMore: Boolean = false,
    val loadingMore: Boolean = false,
    val refreshing: Boolean = false
) {
    val pagingEnabled: Boolean
        get() = contentState === ContentState.Content && hasMore && !loadingMore && !refreshing

    val showMenu: Boolean
        get() = params.username == null && contentState == ContentState.Content

    override fun toString(): String {
        return "CollectionsViewState(" +
                "params=$params," +
                "contentState=$contentState," +
                "signInNeeded=$signInNeeded," +
                "collectionItems=${collectionItems.size}," +
                "items=${items.size}," +
                "snackbarMessage=$snackbarMessage," +
                "hasMore=$hasMore," +
                "loadingMore=$loadingMore," +
                "refreshing=$refreshing" +
                ")"
    }
}
