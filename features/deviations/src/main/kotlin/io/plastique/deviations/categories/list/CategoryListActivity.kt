package io.plastique.deviations.categories.list

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import com.github.technoir42.kotlin.extensions.plus
import com.github.technoir42.rxjava2.extensions.pairwiseWithPrevious
import io.plastique.core.BaseActivity
import io.plastique.core.lists.calculateDiff
import io.plastique.core.mvvm.viewModel
import io.plastique.core.navigation.Route
import io.plastique.core.navigation.activityRoute
import io.plastique.deviations.DeviationsActivityComponent
import io.plastique.deviations.categories.Category
import io.plastique.deviations.categories.list.CategoryListEvent.BreadcrumbClickEvent
import io.plastique.deviations.categories.list.CategoryListEvent.ItemClickEvent
import io.plastique.deviations.categories.list.CategoryListEvent.RetryClickEvent
import io.plastique.deviations.categories.list.CategoryListEvent.SnackbarShownEvent
import io.plastique.inject.getComponent
import io.reactivex.android.schedulers.AndroidSchedulers

class CategoryListActivity : BaseActivity() {
    private val viewModel: CategoryListViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = CategoryListView(
            this,
            onRetryClick = { viewModel.dispatch(RetryClickEvent) },
            onSnackbarShown = { viewModel.dispatch(SnackbarShownEvent) },
            onBreadcrumbClick = { breadcrumb -> viewModel.dispatch(BreadcrumbClickEvent(breadcrumb)) },
            onCategoryClick = { item -> viewModel.dispatch(ItemClickEvent(item)) })

        val parentCategory = intent.getParcelableExtra<Category>(EXTRA_PARENT_CATEGORY)!!
        viewModel.init(parentCategory)
        viewModel.state
            .pairwiseWithPrevious()
            .map { it + calculateDiff(it.second?.items, it.first.items) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { view.render(it.first, it.third) }
            .disposeOnDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
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
