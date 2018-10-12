package io.plastique.deviations.categories.list

import android.os.SystemClock
import com.sch.rxjava2.extensions.ofType
import io.plastique.core.ErrorMessageProvider
import io.plastique.core.ViewModel
import io.plastique.core.breadcrumbs.Breadcrumb
import io.plastique.core.content.ContentState
import io.plastique.core.extensions.replaceIf
import io.plastique.core.flow.MainLoop
import io.plastique.core.flow.Next
import io.plastique.core.flow.Reducer
import io.plastique.core.flow.TimberLogger
import io.plastique.core.flow.next
import io.plastique.deviations.categories.Category
import io.plastique.deviations.categories.CategoryRepository
import io.plastique.deviations.categories.list.CategoryListEffect.LoadCategoryEffect
import io.plastique.deviations.categories.list.CategoryListEvent.BreadcrumbClickEvent
import io.plastique.deviations.categories.list.CategoryListEvent.ErrorShownEvent
import io.plastique.deviations.categories.list.CategoryListEvent.ItemClickEvent
import io.plastique.deviations.categories.list.CategoryListEvent.LoadCategoryErrorEvent
import io.plastique.deviations.categories.list.CategoryListEvent.LoadCategoryFinishEvent
import io.plastique.deviations.categories.list.CategoryListEvent.RetryClickEvent
import io.plastique.inject.scopes.ActivityScope
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

@ActivityScope
class CategoryListViewModel @Inject constructor(
    stateReducer: StateReducer,
    private val categoryRepository: CategoryRepository,
    private val errorMessageProvider: ErrorMessageProvider
) : ViewModel() {

    lateinit var state: Observable<CategoryListViewState>
    private val loop = MainLoop(
            reducer = stateReducer,
            effectHandler = ::effectHandler,
            listener = TimberLogger(LOG_TAG))

    fun init(initialCategory: Category) {
        if (::state.isInitialized) return

        val initialState = CategoryListViewState(
                parent = initialCategory,
                contentState = ContentState.Loading,
                breadcrumbs = createBreadcrumbs(initialCategory))

        state = loop.loop(initialState, LoadCategoryEffect(initialCategory)).disposeOnDestroy()
    }

    fun dispatch(event: CategoryListEvent) {
        loop.dispatch(event)
    }

    private fun effectHandler(effects: Observable<CategoryListEffect>): Observable<CategoryListEvent> {
        return effects.ofType<LoadCategoryEffect>()
                .switchMapSingle { effect ->
                    categoryRepository.getCategories(effect.category)
                            .map<CategoryListEvent> { subcategories -> LoadCategoryFinishEvent(effect.category, subcategories) }
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> LoadCategoryErrorEvent(effect.category, errorMessageProvider.getErrorState(error)) }
                }
    }

    companion object {
        private const val LOG_TAG = "CategoryListViewModel"
    }
}

class StateReducer @Inject constructor() : Reducer<CategoryListEvent, CategoryListViewState, CategoryListEffect> {
    override fun invoke(state: CategoryListViewState, event: CategoryListEvent): Next<CategoryListViewState, CategoryListEffect> = when (event) {
        is ItemClickEvent -> {
            if (event.item.parent || !event.item.category.hasChildren) {
                next(state.copy(selectedCategory = event.item.category))
            } else if (!state.expanding) {
                val items = state.items.replaceIf(
                        { item -> item.category == event.item.category },
                        { item -> item.copy(loading = true, startLoadingTimestamp = SystemClock.elapsedRealtime()) })
                next(state.copy(expanding = true, items = items), LoadCategoryEffect(event.item.category))
            } else {
                next(state)
            }
        }

        is BreadcrumbClickEvent -> {
            val category = event.breadcrumb.tag as Category
            if (state.parent != category) {
                next(state.copy(
                        contentState = ContentState.Loading,
                        parent = category,
                        breadcrumbs = createBreadcrumbs(category),
                        items = emptyList()),
                        LoadCategoryEffect(category))
            } else {
                next(state)
            }
        }

        RetryClickEvent -> {
            next(state.copy(contentState = ContentState.Loading), LoadCategoryEffect(state.parent))
        }

        ErrorShownEvent -> {
            next(state.copy(expandCategoryError = false))
        }

        is LoadCategoryFinishEvent -> {
            next(state.copy(
                    contentState = ContentState.Content,
                    parent = event.category,
                    expanding = false,
                    breadcrumbs = createBreadcrumbs(event.category),
                    items = createItems(event.category, event.subcategories)))
        }

        is LoadCategoryErrorEvent -> {
            if (state.contentState === ContentState.Loading) {
                next(state.copy(contentState = ContentState.Empty(event.emptyState, isError = true)))
            } else {
                val items = state.items.replaceIf(
                        { item -> item.category == event.category },
                        { item -> item.copy(loading = false, startLoadingTimestamp = 0) })
                next(state.copy(items = items, expanding = false, expandCategoryError = true))
            }
        }
    }
}

private fun createItems(parentCategory: Category, categories: List<Category>): List<CategoryItem> {
    val items = mutableListOf(CategoryItem(category = parentCategory, parent = true))
    for (category in categories) {
        items.add(CategoryItem(category = category))
    }
    return items
}

private fun createBreadcrumbs(category: Category): List<Breadcrumb> {
    val breadcrumbs = mutableListOf<Breadcrumb>()
    var current: Category? = category
    while (current != null) {
        breadcrumbs.add(Breadcrumb(current.title, current))
        current = current.parent
    }
    breadcrumbs.reverse()
    return breadcrumbs
}