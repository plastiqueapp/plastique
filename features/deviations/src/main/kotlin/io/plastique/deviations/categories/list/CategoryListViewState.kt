package io.plastique.deviations.categories.list

import io.plastique.core.breadcrumbs.Breadcrumb
import io.plastique.core.content.ContentState
import io.plastique.core.snackbar.SnackbarState
import io.plastique.deviations.categories.Category

data class CategoryListViewState(
    val contentState: ContentState,
    val parent: Category,
    val items: List<CategoryItem> = emptyList(),
    val breadcrumbs: List<Breadcrumb> = emptyList(),
    val isExpanding: Boolean = false,
    val snackbarState: SnackbarState? = null,
    val selectedCategory: Category? = null
) {
    override fun toString(): String {
        return "CategoryListViewState(" +
                "contentState=$contentState, " +
                "parent=${parent.path}, " +
                "items=${items.size}, " +
                "breadcrumbs=${breadcrumbs.size}, " +
                "isExpanding=$isExpanding, " +
                "snackbarState=$snackbarState, " +
                "selectedCategory=$selectedCategory" +
                ")"
    }
}
