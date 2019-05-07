package io.plastique.deviations.categories.list

import com.sch.neon.Event
import io.plastique.core.breadcrumbs.Breadcrumb
import io.plastique.deviations.categories.Category

sealed class CategoryListEvent : Event() {
    data class ItemClickEvent(val item: CategoryItem) : CategoryListEvent()
    data class BreadcrumbClickEvent(val breadcrumb: Breadcrumb) : CategoryListEvent()

    data class LoadCategoryFinishEvent(val category: Category, val subcategories: List<Category>) : CategoryListEvent() {
        override fun toString(): String =
                "LoadCategoryFinishEvent(category=$category, subcategories=${subcategories.size})"
    }

    data class LoadCategoryErrorEvent(val category: Category, val error: Throwable) : CategoryListEvent()

    object RetryClickEvent : CategoryListEvent()
    object SnackbarShownEvent : CategoryListEvent()
}
