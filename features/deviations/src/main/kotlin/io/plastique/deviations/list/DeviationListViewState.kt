package io.plastique.deviations.list

import io.plastique.common.ErrorType
import io.plastique.core.content.ContentState
import io.plastique.core.content.EmptyState
import io.plastique.core.lists.PagedListState
import io.plastique.core.snackbar.SnackbarState
import io.plastique.deviations.FetchParams
import io.plastique.deviations.tags.Tag

data class DeviationListViewState(
    val params: FetchParams,

    val contentState: ContentState,
    val layoutMode: LayoutMode,
    val errorType: ErrorType = ErrorType.None,
    val listState: PagedListState = PagedListState.Empty,
    val snackbarState: SnackbarState? = null,
    val showProgressDialog: Boolean = false,
    val tags: List<Tag> = emptyList(),
    val emptyState: EmptyState? = null
)
