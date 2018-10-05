package io.plastique.deviations.categories.list

import io.plastique.core.breadcrumbs.Breadcrumb
import io.plastique.core.content.ContentState
import io.plastique.deviations.categories.Category

data class CategoryListViewState(
    val contentState: ContentState,
    val parent: Category,
    val items: List<CategoryItem> = emptyList(),
    val breadcrumbs: List<Breadcrumb> = emptyList(),
    val expanding: Boolean = false,
    val expandCategoryError: Boolean = false,
    val selectedCategory: Category? = null
) {
    override fun toString(): String {
        return "CategoryListViewState(contentState=$contentState, parent=${parent.path}, items=${items.size}, breadcrumbs=${breadcrumbs.size}, expanding=$expanding, expandCategoryError=$expandCategoryError, selectedCategory=$selectedCategory)"
    }
}
