package io.plastique.collections

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deleted_collection_folders")
data class DeletedFolderEntity(
    @PrimaryKey
    @ColumnInfo(name = "folder_id")
    val folderId: String
)
