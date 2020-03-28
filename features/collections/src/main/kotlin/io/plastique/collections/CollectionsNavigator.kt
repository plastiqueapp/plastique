package io.plastique.collections

import io.plastique.collections.folders.CollectionFolderId
import io.plastique.core.navigation.Navigator

interface CollectionsNavigator : Navigator {
    fun openCollectionFolder(folderId: CollectionFolderId, folderName: String)

    fun openDeviation(deviationId: String)

    fun openSignIn()
}
