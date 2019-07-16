package io.plastique.collections

import io.plastique.collections.deviations.FolderDeviationListActivity

interface CollectionsActivityComponent {
    fun inject(activity: FolderDeviationListActivity)
}
