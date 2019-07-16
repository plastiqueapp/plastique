package io.plastique.gallery

import io.plastique.gallery.deviations.FolderDeviationListFragment

interface GalleryFragmentComponent {
    fun inject(fragment: GalleryFragment)

    fun inject(fragment: FolderDeviationListFragment)
}
