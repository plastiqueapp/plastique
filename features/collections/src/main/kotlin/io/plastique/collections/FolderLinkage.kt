package io.plastique.collections

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import io.plastique.users.UserEntity

@Entity(tableName = "user_collection_folders",
        primaryKeys = ["user_id", "folder_id"],
        foreignKeys = [
            ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["user_id"], onDelete = ForeignKey.CASCADE),
            ForeignKey(entity = FolderEntity::class, parentColumns = ["id"], childColumns = ["folder_id"], onDelete = ForeignKey.CASCADE)
        ],
        indices = [
            Index("folder_id")
        ])
data class FolderLinkage(
    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "folder_id")
    val folderId: String,

    @ColumnInfo(name = "order")
    val order: Int
)
