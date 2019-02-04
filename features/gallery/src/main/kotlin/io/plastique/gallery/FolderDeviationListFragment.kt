package io.plastique.gallery

import android.os.Bundle
import io.plastique.core.extensions.args
import io.plastique.deviations.list.BaseDeviationListFragment
import io.plastique.inject.getComponent

class FolderDeviationListFragment : BaseDeviationListFragment<GalleryDeviationParams>() {
    override val defaultParams: GalleryDeviationParams
        get() = GalleryDeviationParams(folderId = args.getParcelable(ARG_FOLDER_ID)!!)

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
