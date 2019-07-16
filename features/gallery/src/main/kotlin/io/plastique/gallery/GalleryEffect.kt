package io.plastique.gallery

import com.sch.neon.Effect
import io.plastique.gallery.folders.FolderLoadParams

sealed class GalleryEffect : Effect() {
    data class LoadGalleryEffect(val params: FolderLoadParams) : GalleryEffect()
    object LoadMoreEffect : GalleryEffect()
    object RefreshEffect : GalleryEffect()

    data class CreateFolderEffect(val folderName: String) : GalleryEffect()

    data class DeleteFolderEffect(val folderId: String, val folderName: String) : GalleryEffect()
    data class UndoDeleteFolderEffect(val folderId: String) : GalleryEffect()
}
