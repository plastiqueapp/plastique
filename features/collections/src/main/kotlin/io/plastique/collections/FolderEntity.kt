package io.plastique.collections

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.plastique.api.collections.Folder

@Entity(tableName = "collection_folders")
data class FolderEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "size")
    val size: Int,

    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUrl: String? = null
)

fun Folder.toFolderEntity(): FolderEntity {
    val thumbnailUrl = deviations.asSequence()
            .map { deviation -> deviation.preview?.url ?: deviation.content?.url }
            .firstOrNull()
    return FolderEntity(id = id, name = name, size = size, thumbnailUrl = thumbnailUrl)
}
