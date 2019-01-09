package io.plastique.deviations

import io.plastique.deviations.categories.list.CategoryListActivity
import io.plastique.deviations.info.DeviationInfoActivity
import io.plastique.deviations.viewer.DeviationViewerActivity

interface DeviationsActivityComponent {
    fun inject(activity: CategoryListActivity)

    fun inject(activity: DeviationInfoActivity)

    fun inject(activity: DeviationViewerActivity)
}
