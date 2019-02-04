package io.plastique.collections

import android.os.Bundle
import io.plastique.core.extensions.args
import io.plastique.deviations.list.BaseDeviationListFragment
import io.plastique.inject.getComponent

class FolderDeviationListFragment : BaseDeviationListFragment<CollectionDeviationParams>() {
    override val defaultParams: CollectionDeviationParams
        get() = CollectionDeviationParams(folderId = args.getParcelable(ARG_FOLDER_ID)!!)

    override fun injectDependencies() {
        getComponent<CollectionsFragmentComponent>().inject(this)
    }

    companion object {
        private const val ARG_FOLDER_ID = "folder_id"

        fun newArgs(folderId: CollectionFolderId): Bundle {
            return Bundle().apply {
                putParcelable(ARG_FOLDER_ID, folderId)
            }
        }
    }
}
