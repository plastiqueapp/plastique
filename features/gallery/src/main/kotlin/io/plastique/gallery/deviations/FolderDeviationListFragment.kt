package io.plastique.gallery.deviations

import android.os.Bundle
import io.plastique.deviations.list.BaseDeviationListFragment
import io.plastique.deviations.list.LayoutMode
import io.plastique.gallery.GalleryFragmentComponent
import io.plastique.gallery.folders.GalleryFolderId
import io.plastique.inject.getComponent

class FolderDeviationListFragment : BaseDeviationListFragment<GalleryDeviationParams>() {
    override val defaultParams: GalleryDeviationParams
        get() = GalleryDeviationParams(folderId = requireArguments().getParcelable(ARG_FOLDER_ID)!!)
    override val fixedLayoutMode: LayoutMode? get() = LayoutMode.Grid

    override fun injectDependencies() {
        getComponent<GalleryFragmentComponent>().inject(this)
    }

    companion object {
        private const val ARG_FOLDER_ID = "folder_id"

        fun newArgs(folderId: GalleryFolderId): Bundle {
            return Bundle().apply {
                putParcelable(ARG_FOLDER_ID, folderId)
            }
        }
    }
}
