package io.plastique.deviations.categories.list

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.technoir42.android.extensions.disableChangeAnimations
import com.github.technoir42.android.extensions.setActionBar
import io.plastique.core.breadcrumbs.OnBreadcrumbClickListener
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.OnButtonClickListener
import io.plastique.core.lists.ListUpdateData
import io.plastique.core.snackbar.OnSnackbarShownListener
import io.plastique.core.snackbar.SnackbarController
import io.plastique.deviations.databinding.ActivityCategoryListBinding

internal class CategoryListView(
    private val activity: AppCompatActivity,
    onRetryClick: OnButtonClickListener,
    onSnackbarShown: OnSnackbarShownListener,
    onBreadcrumbClick: OnBreadcrumbClickListener,
    onCategoryClick: OnCategoryClickListener
) {
    private val binding = ActivityCategoryListBinding.inflate(activity.layoutInflater)
    private val categoryListAdapter = CategoryListAdapter(onCategoryClick)
    private val contentStateController: ContentStateController
    private val snackbarController: SnackbarController

    init {
        activity.setContentView(binding.root)
        activity.setActionBar(binding.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }

        binding.categories.apply {
            adapter = categoryListAdapter
            layoutManager = LinearLayoutManager(context)
            disableChangeAnimations()
        }

        binding.breadcrumbs.onBreadcrumbClick = onBreadcrumbClick
        binding.empty.onButtonClick = onRetryClick

        contentStateController = ContentStateController(activity, binding.categories, binding.progress, binding.empty)
        snackbarController = SnackbarController(binding.categories)
        snackbarController.onSnackbarShown = onSnackbarShown
    }

    fun render(state: CategoryListViewState, listUpdateData: ListUpdateData<CategoryItem>) {
        contentStateController.state = state.contentState
        binding.empty.state = state.emptyState
        binding.breadcrumbs.breadcrumbs = state.breadcrumbs
        state.snackbarState?.let(snackbarController::showSnackbar)

        listUpdateData.applyTo(categoryListAdapter)

        if (state.selectedCategory != null) {
            activity.setResult(Activity.RESULT_OK, Intent().putExtra(CategoryListActivity.RESULT_SELECTED_CATEGORY, state.selectedCategory))
            activity.finish()
        }
    }
}
