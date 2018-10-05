package io.plastique.collections

import io.plastique.core.navigation.NavigationContext

interface CollectionsNavigator {
    fun openCollectionFolder(navigationContext: NavigationContext, username: String?, folderId: String, folderName: String)

    fun openDeviation(navigationContext: NavigationContext, deviationId: String)

    fun openLogin(navigationContext: NavigationContext)
}
