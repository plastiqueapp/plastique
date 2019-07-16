package io.plastique.collections

import io.plastique.collections.folders.FolderLoadParams
import io.plastique.core.content.ContentState
import io.plastique.core.lists.PagedListState
import io.plastique.core.snackbar.SnackbarState

data class CollectionsViewState(
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
