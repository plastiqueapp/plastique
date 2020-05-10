package io.plastique.deviations.list

import android.app.Activity
import android.content.Intent
import io.plastique.deviations.DeviationsFragmentComponent
import io.plastique.deviations.UndiscoveredParams
import io.plastique.deviations.categories.Category
import io.plastique.deviations.categories.list.CategoryListActivity
import io.plastique.deviations.tags.Tag
import io.plastique.inject.getComponent

class UndiscoveredDeviationsFragment : BaseDeviationListFragment<UndiscoveredParams>() {
    override val defaultParams: UndiscoveredParams get() = UndiscoveredParams()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_CATEGORY && resultCode == Activity.RESULT_OK) {
            val category = data?.getParcelableExtra<Category>(CategoryListActivity.RESULT_SELECTED_CATEGORY) ?: return
            updateParams(params.copy(category = category, categoryPath = category.path))
        }
    }

    override fun onTagClick(tag: Tag) {
        viewModel.navigator.openCategoryList(tag.payload as Category, REQUEST_CODE_SELECT_CATEGORY)
    }

    override fun injectDependencies() {
        getComponent<DeviationsFragmentComponent>().inject(this)
    }

    companion object {
        private const val REQUEST_CODE_SELECT_CATEGORY = 0
    }
}
