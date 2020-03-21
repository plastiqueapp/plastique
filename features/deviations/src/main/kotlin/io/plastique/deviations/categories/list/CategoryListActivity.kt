package io.plastique.deviations.categories.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.technoir42.android.extensions.disableChangeAnimations
import com.github.technoir42.android.extensions.setActionBar
import com.github.technoir42.kotlin.extensions.plus
import com.github.technoir42.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.core.BaseActivity
import io.plastique.core.content.ContentStateController
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.lists.calculateDiff
import io.plastique.core.mvvm.viewModel
import io.plastique.core.navigation.Route
import io.plastique.core.navigation.activityRoute
import io.plastique.core.snackbar.SnackbarController
import io.plastique.deviations.DeviationsActivityComponent
import io.plastique.deviations.categories.Category
import io.plastique.deviations.categories.list.CategoryListEvent.BreadcrumbClickEvent
import io.plastique.deviations.categories.list.CategoryListEvent.ItemClickEvent
import io.plastique.deviations.categories.list.CategoryListEvent.RetryClickEvent
import io.plastique.deviations.categories.list.CategoryListEvent.SnackbarShownEvent
import io.plastique.deviations.databinding.ActivityCategoryListBinding
import io.plastique.inject.getComponent
import io.reactivex.android.schedulers.AndroidSchedulers

class CategoryListActivity : BaseActivity() {
    private val viewModel: CategoryListViewModel by viewModel()

    private lateinit var binding: ActivityCategoryListBinding
    private lateinit var categoryListAdapter: CategoryListAdapter
    private lateinit var contentStateController: ContentStateController
    private lateinit var snackbarController: SnackbarController

    private val parentCategory: Category
        get() = intent.getParcelableExtra(EXTRA_PARENT_CATEGORY)!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setActionBar(binding.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }

        categoryListAdapter = CategoryListAdapter(
            onCategoryClick = { item -> viewModel.dispatch(ItemClickEvent(item)) })

        binding.categories.apply {
            adapter = categoryListAdapter
            layoutManager = LinearLayoutManager(context)
            disableChangeAnimations()
        }

        binding.breadcrumbs.onBreadcrumbClick = { breadcrumb -> viewModel.dispatch(BreadcrumbClickEvent(breadcrumb)) }
        binding.empty.onButtonClick = { viewModel.dispatch(RetryClickEvent) }

        contentStateController = ContentStateController(this, binding.categories, binding.progress, binding.empty)
        snackbarController = SnackbarController(binding.categories)
        snackbarController.onSnackbarShown = { viewModel.dispatch(SnackbarShownEvent) }

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
        binding.empty.state = state.emptyState
        binding.breadcrumbs.breadcrumbs = state.breadcrumbs
        state.snackbarState?.let(snackbarController::showSnackbar)

        listUpdateData.applyTo(categoryListAdapter)

        if (state.selectedCategory != null) {
            setResult(RESULT_OK, Intent().putExtra(RESULT_SELECTED_CATEGORY, state.selectedCategory))
            finish()
        }
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
