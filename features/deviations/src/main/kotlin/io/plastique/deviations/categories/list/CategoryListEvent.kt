package io.plastique.deviations.categories.list

import io.plastique.core.breadcrumbs.Breadcrumb
import io.plastique.core.content.EmptyState
import io.plastique.core.flow.Event
import io.plastique.deviations.categories.Category

sealed class CategoryListEvent : Event() {
    data class ItemClickEvent(val item: CategoryItem) : CategoryListEvent()
    data class BreadcrumbClickEvent(val breadcrumb: Breadcrumb) : CategoryListEvent()

    object RetryClickEvent : CategoryListEvent()
    object ErrorShownEvent : CategoryListEvent()

    data class LoadCategoryFinishEvent(val category: Category, val subcategories: List<Category>) : CategoryListEvent() {
        override fun toString(): String = "LoadCategoryFinishEvent(category=$category, subcategories=${subcategories.size})"
    }

    data class LoadCategoryErrorEvent(
        val category: Category,
        val emptyState: EmptyState
    ) : CategoryListEvent()
}
