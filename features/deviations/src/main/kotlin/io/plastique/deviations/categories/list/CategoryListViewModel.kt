package io.plastique.deviations.categories.list

import android.os.SystemClock
import com.sch.neon.EffectHandler
import com.sch.neon.MainLoop
import com.sch.neon.StateReducer
import com.sch.neon.StateWithEffects
import com.sch.neon.next
import com.sch.neon.timber.TimberLogger
import io.plastique.common.ErrorMessageProvider
import io.plastique.core.BaseViewModel
import io.plastique.core.breadcrumbs.Breadcrumb
import io.plastique.core.content.ContentState
import io.plastique.core.extensions.replaceIf
import io.plastique.core.snackbar.SnackbarState
import io.plastique.deviations.R
import io.plastique.deviations.categories.Category
import io.plastique.deviations.categories.CategoryRepository
import io.plastique.deviations.categories.list.CategoryListEffect.LoadCategoryEffect
import io.plastique.deviations.categories.list.CategoryListEvent.BreadcrumbClickEvent
import io.plastique.deviations.categories.list.CategoryListEvent.ItemClickEvent
import io.plastique.deviations.categories.list.CategoryListEvent.LoadCategoryErrorEvent
import io.plastique.deviations.categories.list.CategoryListEvent.LoadCategoryFinishEvent
import io.plastique.deviations.categories.list.CategoryListEvent.RetryClickEvent
import io.plastique.deviations.categories.list.CategoryListEvent.SnackbarShownEvent
import io.plastique.inject.scopes.ActivityScope
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import timber.log.Timber
import javax.inject.Inject

@ActivityScope
class CategoryListViewModel @Inject constructor(
    stateReducer: CategoryListStateReducer,
    effectHandler: CategoryListEffectHandler
) : BaseViewModel() {

    lateinit var state: Observable<CategoryListViewState>
    private val loop = MainLoop(
        reducer = stateReducer,
        effectHandler = effectHandler,
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

    companion object {
        private const val LOG_TAG = "CategoryListViewModel"
    }
}

class CategoryListEffectHandler @Inject constructor(
    private val categoryRepository: CategoryRepository
) : EffectHandler<CategoryListEffect, CategoryListEvent> {

    override fun handle(effects: Observable<CategoryListEffect>): Observable<CategoryListEvent> {
        return effects.ofType<LoadCategoryEffect>()
            .switchMapSingle { effect ->
                categoryRepository.getCategories(effect.category)
                    .map<CategoryListEvent> { subcategories -> LoadCategoryFinishEvent(effect.category, subcategories) }
                    .doOnError(Timber::e)
                    .onErrorReturn { error -> LoadCategoryErrorEvent(effect.category, error) }
            }
    }
}

class CategoryListStateReducer @Inject constructor(
    private val errorMessageProvider: ErrorMessageProvider
) : StateReducer<CategoryListEvent, CategoryListViewState, CategoryListEffect> {

    override fun reduce(state: CategoryListViewState, event: CategoryListEvent): StateWithEffects<CategoryListViewState, CategoryListEffect> = when (event) {
        is ItemClickEvent -> {
            if (event.item.parent || !event.item.category.hasChildren) {
                next(state.copy(selectedCategory = event.item.category))
            } else if (!state.isExpanding) {
                val items = state.items.replaceIf(
                    { item -> item.category == event.item.category },
                    { item -> item.copy(loading = true, startLoadingTimestamp = SystemClock.elapsedRealtime()) })
                next(state.copy(isExpanding = true, items = items), LoadCategoryEffect(event.item.category))
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

        is LoadCategoryFinishEvent -> {
            next(state.copy(
                contentState = ContentState.Content,
                parent = event.category,
                isExpanding = false,
                breadcrumbs = createBreadcrumbs(event.category),
                items = createItems(event.category, event.subcategories)))
        }

        is LoadCategoryErrorEvent -> {
            if (state.contentState === ContentState.Loading) {
                next(state.copy(contentState = ContentState.Empty(isError = true, emptyState = errorMessageProvider.getErrorState(event.error))))
            } else {
                val items = state.items.replaceIf(
                    { item -> item.category == event.category },
                    { item -> item.copy(loading = false, startLoadingTimestamp = 0) })
                next(state.copy(items = items,
                    isExpanding = false,
                    snackbarState = SnackbarState.Message(R.string.deviations_categories_load_error)))
            }
        }

        RetryClickEvent -> {
            next(state.copy(contentState = ContentState.Loading), LoadCategoryEffect(state.parent))
        }

        SnackbarShownEvent -> {
            next(state.copy(snackbarState = SnackbarState.None))
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
