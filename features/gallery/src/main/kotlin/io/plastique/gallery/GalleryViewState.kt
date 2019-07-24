package io.plastique.gallery

import io.plastique.core.content.ContentState
import io.plastique.core.content.EmptyState
import io.plastique.core.lists.PagedListState
import io.plastique.core.snackbar.SnackbarState
import io.plastique.gallery.folders.FolderLoadParams

data class GalleryViewState(
    val params: FolderLoadParams,

    val contentState: ContentState,
    val signInNeeded: Boolean,
    val listState: PagedListState = PagedListState.Empty,
    val showProgressDialog: Boolean = false,
    val snackbarState: SnackbarState? = null,
    val emptyState: EmptyState? = null
) {
    val showMenu: Boolean
        get() = params.username == null && contentState == ContentState.Content
}
