package io.plastique.gallery

data class Folder(
    val id: String,
    val name: String,
    val size: Int,
    val thumbnailUrl: String?
) {
    val isDeletable: Boolean
        get() = name != FEATURED

    companion object {
        const val FEATURED = "Featured"
    }
}

fun FolderEntity.toFolder(): Folder =
        Folder(id = id, name = name, size = size, thumbnailUrl = thumbnailUrl)
