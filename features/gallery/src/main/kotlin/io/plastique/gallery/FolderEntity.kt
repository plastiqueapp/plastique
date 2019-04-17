package io.plastique.gallery

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gallery_folders")
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
