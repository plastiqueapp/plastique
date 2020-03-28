package io.plastique.collections.folders

data class Folder(
    val id: CollectionFolderId,
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

fun FolderEntity.toFolder(owner: String, own: Boolean = false): Folder = Folder(
    id = CollectionFolderId(id = id, owner = owner),
    name = name,
    size = size,
    thumbnailUrl = thumbnailUrl,
    isDeletable = own && name != Folder.FEATURED)
