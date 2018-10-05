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
    override val defaultParams get() = UndiscoveredParams()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_CATEGORY && resultCode == Activity.RESULT_OK && data != null) {
            val category = data.getSerializableExtra(CategoryListActivity.RESULT_SELECTED_CATEGORY) as Category
            setNewParams(params.copy(category = category))
        }
    }

    override fun onTagClick(tag: Tag) {
        val intent = CategoryListActivity.createIntent(requireContext(), tag.value as Category)
        startActivityForResult(intent, REQUEST_CODE_SELECT_CATEGORY)
    }

    override fun injectDependencies() {
        getComponent<DeviationsFragmentComponent>().inject(this)
    }

    companion object {
        private const val REQUEST_CODE_SELECT_CATEGORY = 0
    }
}
