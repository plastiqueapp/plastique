package io.plastique.deviations.categories.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.technoir42.android.extensions.setActionBar
import com.github.technoir42.kotlin.extensions.plus
import com.github.technoir42.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.core.BaseActivity
import io.plastique.core.breadcrumbs.BreadcrumbsView
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.EmptyView
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.lists.calculateDiff
import io.plastique.core.mvvm.viewModel
import io.plastique.core.navigation.Route
import io.plastique.core.navigation.activityRoute
import io.plastique.core.snackbar.SnackbarController
import io.plastique.deviations.DeviationsActivityComponent
import io.plastique.deviations.R
import io.plastique.deviations.categories.Category
import io.plastique.deviations.categories.list.CategoryListEvent.BreadcrumbClickEvent
import io.plastique.deviations.categories.list.CategoryListEvent.ItemClickEvent
import io.plastique.deviations.categories.list.CategoryListEvent.RetryClickEvent
import io.plastique.deviations.categories.list.CategoryListEvent.SnackbarShownEvent
import io.plastique.inject.getComponent
import io.reactivex.android.schedulers.AndroidSchedulers

class CategoryListActivity : BaseActivity(R.layout.activity_category_list) {
    private val viewModel: CategoryListViewModel by viewModel()

    private lateinit var categoriesView: RecyclerView
    private lateinit var breadcrumbsView: BreadcrumbsView
    private lateinit var emptyView: EmptyView
    private lateinit var contentStateController: ContentStateController
    private lateinit var snackbarController: SnackbarController
    private lateinit var adapter: CategoryListAdapter

    private val parentCategory: Category
        get() = intent.getParcelableExtra(EXTRA_PARENT_CATEGORY)!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }

        breadcrumbsView = findViewById(R.id.breadcrumbs)
        breadcrumbsView.onBreadcrumbClick = { breadcrumb -> viewModel.dispatch(BreadcrumbClickEvent(breadcrumb)) }

        adapter = CategoryListAdapter(
            onCategoryClick = { item -> viewModel.dispatch(ItemClickEvent(item)) })
        categoriesView = findViewById(R.id.categories)
        categoriesView.layoutManager = LinearLayoutManager(this)
        categoriesView.adapter = adapter

        contentStateController = ContentStateController(this, R.id.categories, android.R.id.progress, android.R.id.empty)
        snackbarController = SnackbarController(categoriesView)
        snackbarController.onSnackbarShown = { viewModel.dispatch(SnackbarShownEvent) }

        emptyView = findViewById(android.R.id.empty)
        emptyView.onButtonClick = { viewModel.dispatch(RetryClickEvent) }

        viewModel.init(parentCategory)
        viewModel.state
            .pairwiseWithPrevious()
            .map { it + calculateDiff(it.second?.items, it.first.items) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { renderState(it.first, it.third) }
            .disposeOnDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun renderState(state: CategoryListViewState, listUpdateData: ListUpdateData<CategoryItem>) {
        contentStateController.state = state.contentState
        emptyView.state = state.emptyState
        breadcrumbsView.breadcrumbs = state.breadcrumbs

        listUpdateData.applyTo(adapter)

        if (state.selectedCategory != null) {
            setResult(RESULT_OK, Intent().putExtra(RESULT_SELECTED_CATEGORY, state.selectedCategory))
            finish()
        }

        state.snackbarState?.let(snackbarController::showSnackbar)
    }

    override fun injectDependencies() {
        getComponent<DeviationsActivityComponent>().inject(this)
    }

    companion object {
        private const val EXTRA_PARENT_CATEGORY = "parent_category"
        const val RESULT_SELECTED_CATEGORY = "selected_category"

        fun route(context: Context, requestCode: Int, selectedCategory: Category): Route =
            activityRoute<CategoryListActivity>(context, requestCode = requestCode) {
                val parentCategory = if (selectedCategory.hasChildren) selectedCategory else selectedCategory.parent!!
                putExtra(EXTRA_PARENT_CATEGORY, parentCategory)
            }
    }
}
