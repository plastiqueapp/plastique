package io.plastique.gallery

import io.plastique.core.navigation.NavigationContext
import io.plastique.gallery.folders.GalleryFolderId

interface GalleryNavigator {
    fun openGalleryFolder(navigationContext: NavigationContext, folderId: GalleryFolderId, folderName: String)

    fun openDeviation(navigationContext: NavigationContext, deviationId: String)

    fun openLogin(navigationContext: NavigationContext)
}
