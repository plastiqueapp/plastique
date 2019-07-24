package io.plastique.statuses.list

import io.plastique.core.content.ContentState
import io.plastique.core.content.EmptyState
import io.plastique.core.lists.PagedListState
import io.plastique.core.snackbar.SnackbarState
import io.plastique.statuses.StatusListLoadParams

data class StatusListViewState(
    val params: StatusListLoadParams,

    val contentState: ContentState,
    val listState: PagedListState = PagedListState.Empty,
    val snackbarState: SnackbarState? = null,
    val emptyState: EmptyState? = null
)
