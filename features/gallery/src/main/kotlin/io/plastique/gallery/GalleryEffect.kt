package io.plastique.gallery

import io.plastique.core.flow.Effect

sealed class GalleryEffect : Effect() {
    data class LoadGalleryEffect(val params: FolderLoadParams) : GalleryEffect()
    object LoadMoreEffect : GalleryEffect()
    object RefreshEffect : GalleryEffect()
}
