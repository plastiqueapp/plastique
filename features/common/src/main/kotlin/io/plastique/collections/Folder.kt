package io.plastique.collections

data class Folder(
    val id: String,
    val name: String,
    val size: Int,
    val thumbnailUrl: String?,
    val isDeletable: Boolean
) {
    companion object {
        const val FEATURED = "Featured"
    }
}

fun FolderEntity.toFolder(own: Boolean = false): Folder = Folder(
    id = id,
    name = name,
    size = size,
    thumbnailUrl = thumbnailUrl,
    isDeletable = own && name != Folder.FEATURED)
