package io.plastique.gallery

import io.plastique.core.content.ContentState
import io.plastique.core.lists.ListItem
import io.plastique.core.snackbar.SnackbarState

data class GalleryViewState(
    val params: FolderLoadParams,

    val contentState: ContentState,
    val signInNeeded: Boolean,
    val items: List<ListItem> = emptyList(),
    val galleryItems: List<ListItem> = emptyList(),
    val snackbarState: SnackbarState = SnackbarState.None,

    val hasMore: Boolean = false,
    val loadingMore: Boolean = false,
    val isRefreshing: Boolean = false
) {
    val pagingEnabled: Boolean
        get() = contentState === ContentState.Content && hasMore && !loadingMore && !isRefreshing

    val showMenu: Boolean
        get() = params.username == null && contentState == ContentState.Content

    override fun toString(): String {
        return "GalleryViewState(" +
                "params=$params, " +
                "contentState=$contentState, " +
                "signInNeeded=$signInNeeded, " +
                "items=${items.size}, " +
                "galleryItems=${galleryItems.size}, " +
                "snackbarState=$snackbarState, " +
                "hasMore=$hasMore, " +
                "loadingMore=$loadingMore, " +
                "isRefreshing=$isRefreshing" +
                ")"
    }
}
