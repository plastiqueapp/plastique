package io.plastique.collections.folders

data class Folder(
    val id: String,
    val name: String,
    val owner: String,
    val size: Int,
    val thumbnailUrl: String?,
    val isDeletable: Boolean
) {
    val isNotEmpty: Boolean get() = size != 0

    companion object {
        const val FEATURED = "Featured"
    }
}

fun FolderEntity.toFolder(owner: String, own: Boolean = false): Folder = Folder(
    id = id,
    name = name,
    owner = owner,
    size = size,
    thumbnailUrl = thumbnailUrl,
    isDeletable = own && name != Folder.FEATURED)
