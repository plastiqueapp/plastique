package io.plastique.collections

import io.plastique.core.navigation.Navigator

interface CollectionsNavigator : Navigator {
    fun openCollectionFolder(username: String?, folderId: String, folderName: String)

    fun openDeviation(deviationId: String)

    fun openSignIn()
}
