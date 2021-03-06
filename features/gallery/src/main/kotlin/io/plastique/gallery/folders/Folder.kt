package io.plastique.gallery.folders

data class Folder(
    val id: GalleryFolderId,
    val name: String,
    val size: Int,
    val thumbnailUrl: String?,
    val isDeletable: Boolean
) {
    val isNotEmpty: Boolean get() = size != 0

    companion object {
        const val FEATURED = "Featured"
    }
}
