package io.plastique.gallery

interface GalleryActivityComponent {
    fun inject(activity: GalleryActivity)

    fun inject(activity: FolderDeviationListActivity)
}
