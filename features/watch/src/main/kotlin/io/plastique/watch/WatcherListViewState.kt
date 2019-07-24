package io.plastique.watch

import io.plastique.common.ErrorType
import io.plastique.core.content.ContentState
import io.plastique.core.content.EmptyState
import io.plastique.core.lists.PagedListState
import io.plastique.core.snackbar.SnackbarState

data class WatcherListViewState(
    val username: String?,

    val contentState: ContentState,
    val errorType: ErrorType = ErrorType.None,
    val listState: PagedListState = PagedListState.Empty,
    val signInNeeded: Boolean,
    val snackbarState: SnackbarState? = null,
    val emptyState: EmptyState? = null
)
