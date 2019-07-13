package io.plastique.core.lists

data class PagedListState internal constructor(
    val items: List<ListItem>,
    val contentItems: List<ListItem>,
    val hasMore: Boolean,
    val isLoadingMore: Boolean,
    val isRefreshing: Boolean
) {
    val isPagingEnabled: Boolean
        get() = hasMore && !isLoadingMore && !isRefreshing

    override fun toString(): String {
        return "PagedListState(" +
                "items=${items.size}, " +
                "contentItems=${contentItems.size}, " +
                "hasMore=$hasMore, " +
                "isLoadingMore=$isLoadingMore, " +
                "isRefreshing=$isRefreshing" +
                ")"
    }

    companion object {
        val Empty = PagedListState(
            items = emptyList(),
            contentItems = emptyList(),
            hasMore = false,
            isLoadingMore = false,
            isRefreshing = false)
    }
}
