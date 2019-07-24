package io.plastique.notifications

import io.plastique.core.content.ContentState
import io.plastique.core.content.EmptyState
import io.plastique.core.lists.PagedListState
import io.plastique.core.snackbar.SnackbarState

data class NotificationsViewState(
    val contentState: ContentState,
    val isSignedIn: Boolean,
    val listState: PagedListState = PagedListState.Empty,
    val snackbarState: SnackbarState? = null,
    val emptyState: EmptyState? = null
)
