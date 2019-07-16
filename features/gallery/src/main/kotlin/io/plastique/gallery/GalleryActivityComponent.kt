package io.plastique.gallery

import io.plastique.gallery.deviations.FolderDeviationListActivity

interface GalleryActivityComponent {
    fun inject(activity: FolderDeviationListActivity)
}
