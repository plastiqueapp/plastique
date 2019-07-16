package io.plastique.collections

import io.plastique.collections.deviations.FolderDeviationListFragment

interface CollectionsFragmentComponent {
    fun inject(fragment: CollectionsFragment)

    fun inject(fragment: FolderDeviationListFragment)
}
