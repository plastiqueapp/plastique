package io.plastique.deviations.list

import io.plastique.core.content.ContentState
import io.plastique.core.lists.ListItem
import io.plastique.deviations.FetchParams
import io.plastique.deviations.tags.Tag

data class DeviationListViewState(
    val params: FetchParams,

    val contentState: ContentState = ContentState.None,
    val items: List<ListItem> = emptyList(),
    val deviationItems: List<ListItem> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val snackbarMessage: String? = null,
    val layoutMode: LayoutMode,

    val hasMore: Boolean = false,
    val loadingMore: Boolean = false,
    val refreshing: Boolean = false
) {
    val pagingEnabled: Boolean
        get() = contentState === ContentState.Content && hasMore && !loadingMore && !refreshing

    override fun toString(): String {
        return "DeviationListViewState(" +
                "params=$params, " +
                "contentState=$contentState, " +
                "items=${items.size}, " +
                "deviationItems=${deviationItems.size}, " +
                "tags=${tags.size}, " +
                "hasMore=$hasMore, " +
                "loadingMore=$loadingMore, " +
                "refreshing=$refreshing, " +
                "snackbarMessage=$snackbarMessage, " +
                "layoutMode=$layoutMode" +
                ")"
    }
}