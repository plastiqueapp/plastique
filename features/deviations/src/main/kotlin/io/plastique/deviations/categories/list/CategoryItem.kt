package io.plastique.deviations.categories.list

import io.plastique.core.lists.ListItem
import io.plastique.deviations.categories.Category

data class CategoryItem(
    val category: Category,
    val parent: Boolean = false,
    val loading: Boolean = false,
    val startLoadingTimestamp: Long = 0
) : ListItem {

    override val id: String = category.path + parent
}
