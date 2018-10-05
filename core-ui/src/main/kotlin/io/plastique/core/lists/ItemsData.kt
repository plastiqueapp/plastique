package io.plastique.core.lists

// TODO: Come up with a better name
data class ItemsData(
    val items: List<ListItem>,
    val hasMore: Boolean = false
)
