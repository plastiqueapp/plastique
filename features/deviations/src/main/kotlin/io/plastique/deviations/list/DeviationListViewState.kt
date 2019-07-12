package io.plastique.deviations.list

import io.plastique.core.content.ContentState
import io.plastique.core.lists.ListItem
import io.plastique.core.snackbar.SnackbarState
import io.plastique.deviations.FetchParams
import io.plastique.deviations.tags.Tag

data class DeviationListViewState(
    val params: FetchParams,

    val contentState: ContentState,
    val items: List<ListItem> = emptyList(),
    val deviationItems: List<ListItem> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val snackbarState: SnackbarState? = null,
    val layoutMode: LayoutMode,

    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val showProgressDialog: Boolean = false
) {
    val isPagingEnabled: Boolean
        get() = contentState === ContentState.Content && hasMore && !isLoadingMore && !isRefreshing

    override fun toString(): String {
        return "DeviationListViewState(" +
                "params=$params, " +
                "contentState=$contentState, " +
                "items=${items.size}, " +
                "deviationItems=${deviationItems.size}, " +
                "tags=${tags.size}, " +
                "snackbarState=$snackbarState, " +
                "layoutMode=$layoutMode, " +
                "hasMore=$hasMore, " +
                "isLoadingMore=$isLoadingMore, " +
                "isRefreshing=$isRefreshing, " +
                "showProgressDialog=$showProgressDialog" +
                ")"
    }
}
