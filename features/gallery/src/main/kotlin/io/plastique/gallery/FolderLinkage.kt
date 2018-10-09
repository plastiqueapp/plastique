package io.plastique.gallery

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import io.plastique.core.cache.CacheEntry

@Entity(tableName = "user_gallery_folders",
        primaryKeys = ["key", "folder_id"],
        foreignKeys = [
            ForeignKey(entity = CacheEntry::class, parentColumns = ["key"], childColumns = ["key"], onDelete = ForeignKey.CASCADE),
            ForeignKey(entity = FolderEntity::class, parentColumns = ["id"], childColumns = ["folder_id"], onDelete = ForeignKey.CASCADE)
        ],
        indices = [
            Index("folder_id")
        ])
data class FolderLinkage(
    @ColumnInfo(name = "key")
    val key: String,

    @ColumnInfo(name = "folder_id")
    val folderId: String,

    @ColumnInfo(name = "order")
    val order: Int
)
