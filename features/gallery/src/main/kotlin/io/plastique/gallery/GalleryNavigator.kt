package io.plastique.gallery

import io.plastique.core.navigation.Navigator
import io.plastique.gallery.folders.GalleryFolderId

interface GalleryNavigator : Navigator {
    fun openGalleryFolder(folderId: GalleryFolderId, folderName: String)

    fun openDeviation(deviationId: String)

    fun openSignIn()
}
