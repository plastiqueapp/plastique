package io.plastique.gallery

data class Folder(
    val id: String,
    val name: String,
    val size: Int,
    val thumbnailUrl: String?
) {
    companion object {
        const val FEATURED = "Featured"
    }
}

fun FolderEntity.toFolder(): Folder =
        Folder(id = id, name = name, size = size, thumbnailUrl = thumbnailUrl)
