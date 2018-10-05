package io.plastique.gallery

import io.plastique.core.navigation.NavigationContext

interface GalleryNavigator {
    fun openGalleryFolder(navigationContext: NavigationContext, username: String?, folderId: String, folderName: String)

    fun openDeviation(navigationContext: NavigationContext, deviationId: String)
}
