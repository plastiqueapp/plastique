package io.plastique.gallery

import io.plastique.core.content.ContentState
import io.plastique.core.lists.PagedListState
import io.plastique.core.snackbar.SnackbarState

data class GalleryViewState(
    val params: FolderLoadParams,

    val contentState: ContentState,
    val signInNeeded: Boolean,
    val listState: PagedListState = PagedListState.Empty,
    val snackbarState: SnackbarState? = null,
    val showProgressDialog: Boolean = false
) {
    val showMenu: Boolean
        get() = params.username == null && contentState === ContentState.Content
}
