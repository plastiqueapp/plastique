package io.plastique.collections

import io.plastique.core.extensions.args
import io.plastique.core.extensions.withArguments
import io.plastique.deviations.list.BaseDeviationListFragment
import io.plastique.inject.getComponent

class FolderDeviationListFragment : BaseDeviationListFragment<CollectionDeviationParams>() {
    override val defaultParams: CollectionDeviationParams
        get() = CollectionDeviationParams(
                username = args.getString(ARG_USERNAME),
                folderId = args.getString(ARG_FOLDER_ID)!!)

    override fun injectDependencies() {
        getComponent<CollectionsFragmentComponent>().inject(this)
    }

    companion object {
        private const val ARG_USERNAME = "username"
        private const val ARG_FOLDER_ID = "folder_id"

        fun newInstance(username: String?, folderId: String) : FolderDeviationListFragment{
            return FolderDeviationListFragment().withArguments {
                putString(ARG_USERNAME, username)
                putString(ARG_FOLDER_ID, folderId)
            }
        }
    }
}
