package io.plastique.feed

import io.plastique.core.content.ContentState
import io.plastique.core.content.EmptyState
import io.plastique.core.lists.PagedListState
import io.plastique.core.snackbar.SnackbarState

data class FeedViewState(
    val contentState: ContentState,
    val isSignedIn: Boolean,
    val showMatureContent: Boolean,
    val listState: PagedListState = PagedListState.Empty,
    val isApplyingSettings: Boolean = false,
    val showProgressDialog: Boolean = false,
    val snackbarState: SnackbarState? = null,
    val emptyState: EmptyState? = null
)
