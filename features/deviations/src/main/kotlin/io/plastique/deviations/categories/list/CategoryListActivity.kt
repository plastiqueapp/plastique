package io.plastique.deviations.categories.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import io.plastique.core.MvvmActivity
import io.plastique.core.breadcrumbs.BreadcrumbsView
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentViewController
import io.plastique.core.content.EmptyView
import io.plastique.core.extensions.setActionBar
import io.plastique.core.lists.ListItemDiffTransformer
import io.plastique.deviations.DeviationsActivityComponent
import io.plastique.deviations.R
import io.plastique.deviations.categories.Category
import io.plastique.deviations.categories.list.CategoryListEvent.BreadcrumbClickEvent
import io.plastique.deviations.categories.list.CategoryListEvent.ErrorShownEvent
import io.plastique.deviations.categories.list.CategoryListEvent.ItemClickEvent
import io.plastique.deviations.categories.list.CategoryListEvent.RetryClickEvent
import io.plastique.inject.getComponent
import io.reactivex.android.schedulers.AndroidSchedulers

class CategoryListActivity : MvvmActivity<CategoryListViewModel>() {
    private lateinit var categoriesView: RecyclerView
    private lateinit var breadcrumbsView: BreadcrumbsView
    private lateinit var emptyView: EmptyView
    private lateinit var contentViewController: ContentViewController
    private lateinit var adapter: CategoriesAdapter

    private val parentCategory: Category
        get() = intent.getSerializableExtra(EXTRA_PARENT_CATEGORY) as Category

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_list)
        setActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }

        breadcrumbsView = findViewById(R.id.breadcrumbs)
        breadcrumbsView.setOnBreadcrumbClickListener { breadcrumb -> viewModel.dispatch(BreadcrumbClickEvent(breadcrumb)) }

        adapter = CategoriesAdapter()
        adapter.onItemClickListener = { item -> viewModel.dispatch(ItemClickEvent(item)) }
        categoriesView = findViewById(R.id.categories)
        categoriesView.layoutManager = LinearLayoutManager(this)
        categoriesView.adapter = adapter

        contentViewController = ContentViewController(this, R.id.categories, android.R.id.progress, android.R.id.empty)

        emptyView = findViewById(android.R.id.empty)
        emptyView.setOnButtonClickListener(View.OnClickListener { viewModel.dispatch(RetryClickEvent) })

        viewModel.init(parentCategory)
        observeState()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun observeState() {
        viewModel.state
                .map { state -> state.contentState }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { contentState ->
                    contentViewController.switchState(contentState)
                    if (contentState is ContentState.Empty) {
                        emptyView.setState(contentState.emptyState)
                    }
                }
                .disposeOnDestroy()

        @Suppress("RemoveExplicitTypeArguments")
        viewModel.state
                .map { state -> state.items }
                .compose(ListItemDiffTransformer<CategoryItem>())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { updateData -> updateData.applyTo(adapter) }
                .disposeOnDestroy()

        viewModel.state
                .map { state -> state.breadcrumbs }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { breadcrumbs ->
                    breadcrumbsView.setBreadcrumbs(breadcrumbs)
                    breadcrumbsView.scrollToPosition(breadcrumbsView.adapter!!.itemCount - 1)
                }
                .disposeOnDestroy()

        viewModel.state
                .filter { state -> state.contentState === ContentState.Content && state.expandCategoryError }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Snackbar.make(categoriesView, R.string.deviations_categories_load_error, Snackbar.LENGTH_LONG).show()
                    viewModel.dispatch(ErrorShownEvent)
                }
                .disposeOnDestroy()

        viewModel.state
                .filter { state -> state.selectedCategory != null }
                .map { state -> state.selectedCategory }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { category ->
                    setResult(RESULT_OK, Intent().putExtra(RESULT_SELECTED_CATEGORY, category))
                    finish()
                }
                .disposeOnDestroy()
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
