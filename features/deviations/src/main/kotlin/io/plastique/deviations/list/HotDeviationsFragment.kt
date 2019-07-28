package io.plastique.deviations.list

import android.app.Activity
import android.content.Intent
import io.plastique.deviations.DeviationsFragmentComponent
import io.plastique.deviations.HotParams
import io.plastique.deviations.categories.Category
import io.plastique.deviations.categories.list.CategoryListActivity
import io.plastique.deviations.tags.Tag
import io.plastique.inject.getComponent

class HotDeviationsFragment : BaseDeviationListFragment<HotParams>() {
    override val defaultParams: HotParams get() = HotParams()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_CATEGORY && resultCode == Activity.RESULT_OK && data != null) {
            val category = data.getParcelableExtra<Category>(CategoryListActivity.RESULT_SELECTED_CATEGORY)
            updateParams(params.copy(category = category, categoryPath = category.path))
        }
    }

    override fun onTagClick(tag: Tag) {
        navigator.openCategoryList(tag.payload as Category, REQUEST_CODE_SELECT_CATEGORY)
    }

    override fun injectDependencies() {
        getComponent<DeviationsFragmentComponent>().inject(this)
    }

    companion object {
        private const val REQUEST_CODE_SELECT_CATEGORY = 0
    }
}
