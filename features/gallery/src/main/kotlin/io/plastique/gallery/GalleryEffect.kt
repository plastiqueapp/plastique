package io.plastique.gallery

import com.sch.neon.Effect

sealed class GalleryEffect : Effect() {
    data class LoadGalleryEffect(val params: FolderLoadParams) : GalleryEffect()
    object LoadMoreEffect : GalleryEffect()
    object RefreshEffect : GalleryEffect()
}
