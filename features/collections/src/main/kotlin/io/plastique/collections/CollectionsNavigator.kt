package io.plastique.collections

import io.plastique.core.navigation.NavigationContext

interface CollectionsNavigator {
    fun openCollectionFolder(navigationContext: NavigationContext, folderId: CollectionFolderId, folderName: String)

    fun openDeviation(navigationContext: NavigationContext, deviationId: String)

    fun openLogin(navigationContext: NavigationContext)
}
