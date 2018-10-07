package io.plastique.collections

import io.plastique.core.extensions.args
import io.plastique.core.extensions.withArguments
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

        fun newInstance(folderId: CollectionFolderId): FolderDeviationListFragment {
            return FolderDeviationListFragment().withArguments {
                putParcelable(ARG_FOLDER_ID, folderId)
            }
        }
    }
}
