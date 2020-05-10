package io.plastique.deviations.list

import android.app.Activity
import android.content.Intent
import io.plastique.deviations.DeviationsFragmentComponent
import io.plastique.deviations.PopularParams
import io.plastique.deviations.TimeRange
import io.plastique.deviations.categories.Category
import io.plastique.deviations.categories.list.CategoryListActivity
import io.plastique.deviations.tags.Tag
import io.plastique.inject.getComponent

class PopularDeviationsFragment : BaseDeviationListFragment<PopularParams>(), OnTimeRangeSelectedListener {
    override val defaultParams: PopularParams get() = PopularParams()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_CATEGORY && resultCode == Activity.RESULT_OK) {
            val category = data?.getParcelableExtra<Category>(CategoryListActivity.RESULT_SELECTED_CATEGORY) ?: return
            updateParams(params.copy(category = category, categoryPath = category.path))
        }
    }

    override fun onTagClick(tag: Tag) {
        when (tag.type) {
            Tag.TYPE_CATEGORY -> viewModel.navigator.openCategoryList(tag.payload as Category, REQUEST_CODE_SELECT_CATEGORY)
            Tag.TYPE_TIME_RANGE -> viewModel.navigator.showTimeRangeDialog(DIALOG_TIME_RANGE)
        }
    }

    override fun onTimeRangeSelected(timeRange: TimeRange) {
        updateParams(params.copy(timeRange = timeRange))
    }

    override fun injectDependencies() {
        getComponent<DeviationsFragmentComponent>().inject(this)
    }

    companion object {
        private const val REQUEST_CODE_SELECT_CATEGORY = 0
        private const val DIALOG_TIME_RANGE = "dialog_time_range"
    }
}
