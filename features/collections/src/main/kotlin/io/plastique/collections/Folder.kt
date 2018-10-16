package io.plastique.collections

data class Folder(
    val id: String,
    val name: String,
    val size: Int,
    val thumbnailUrl: String?
)

fun FolderEntity.toFolder(): Folder =
        Folder(id = id, name = name, size = size, thumbnailUrl = thumbnailUrl)
