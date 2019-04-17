package io.plastique.deviations.categories.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sch.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.core.MvvmActivity
import io.plastique.core.breadcrumbs.BreadcrumbsView
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.EmptyView
import io.plastique.core.extensions.add
import io.plastique.core.extensions.setActionBar
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.lists.calculateDiff
import io.plastique.core.snackbar.SnackbarController
import io.plastique.core.snackbar.SnackbarState
import io.plastique.deviations.DeviationsActivityComponent
import io.plastique.deviations.R
import io.plastique.deviations.categories.Category
import io.plastique.deviations.categories.list.CategoryListEvent.BreadcrumbClickEvent
import io.plastique.deviations.categories.list.CategoryListEvent.ItemClickEvent
import io.plastique.deviations.categories.list.CategoryListEvent.RetryClickEvent
import io.plastique.deviations.categories.list.CategoryListEvent.SnackbarShownEvent
import io.plastique.inject.getComponent
import io.reactivex.android.schedulers.AndroidSchedulers

class CategoryListActivity : MvvmActivity<CategoryListViewModel>() {
    private lateinit var categoriesView: RecyclerView
    private lateinit var breadcrumbsView: BreadcrumbsView
    private lateinit var emptyView: EmptyView
    private lateinit var contentStateController: ContentStateController
    private lateinit var snackbarController: SnackbarController
    private lateinit var adapter: CategoryListAdapter

    private val parentCategory: Category
        get() = intent.getParcelableExtra(EXTRA_PARENT_CATEGORY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_list)
        setActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }

        breadcrumbsView = findViewById(R.id.breadcrumbs)
        breadcrumbsView.setOnBreadcrumbClickListener { breadcrumb -> viewModel.dispatch(BreadcrumbClickEvent(breadcrumb)) }

        adapter = CategoryListAdapter(
                onItemClick = { item -> viewModel.dispatch(ItemClickEvent(item)) })
        categoriesView = findViewById(R.id.categories)
        categoriesView.layoutManager = LinearLayoutManager(this)
        categoriesView.adapter = adapter

        contentStateController = ContentStateController(this, R.id.categories, android.R.id.progress, android.R.id.empty)
        snackbarController = SnackbarController(categoriesView)

        emptyView = findViewById(android.R.id.empty)
        emptyView.setOnButtonClickListener { viewModel.dispatch(RetryClickEvent) }

        viewModel.init(parentCategory)
        viewModel.state
                .pairwiseWithPrevious()
                .map { it.add(calculateDiff(it.second?.items, it.first.items)) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { renderState(it.first, it.second, it.third) }
                .disposeOnDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun renderState(state: CategoryListViewState, prevState: CategoryListViewState?, listUpdateData: ListUpdateData<CategoryItem>) {
        contentStateController.state = state.contentState
        if (state.contentState is ContentState.Empty) {
            emptyView.setState(state.contentState.emptyState)
        }

        listUpdateData.applyTo(adapter)

        if (state.breadcrumbs != prevState?.breadcrumbs) {
            breadcrumbsView.setBreadcrumbs(state.breadcrumbs)
            breadcrumbsView.scrollToPosition(breadcrumbsView.adapter!!.itemCount - 1)
        }

        if (state.selectedCategory != null) {
            setResult(RESULT_OK, Intent().putExtra(RESULT_SELECTED_CATEGORY, state.selectedCategory))
            finish()
        }

        if (state.snackbarState !== SnackbarState.None && state.snackbarState != prevState?.snackbarState) {
            snackbarController.showSnackbar(state.snackbarState)
            viewModel.dispatch(SnackbarShownEvent)
        }
    }

    override fun injectDependencies() {
        getComponent<DeviationsActivityComponent>().inject(this)
    }

    companion object {
        private const val EXTRA_PARENT_CATEGORY = "parent_category"
        const val RESULT_SELECTED_CATEGORY = "selected_category"

        fun createIntent(context: Context, selectedCategory: Category): Intent {
            val parentCategory = if (selectedCategory.hasChildren) selectedCategory else selectedCategory.parent!!
            return Intent(context, CategoryListActivity::class.java).apply {
                putExtra(EXTRA_PARENT_CATEGORY, parentCategory)
            }
        }
    }
}
