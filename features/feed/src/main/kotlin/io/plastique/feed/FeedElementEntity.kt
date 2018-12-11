package io.plastique.feed

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.plastique.collections.FolderEntity
import io.plastique.statuses.StatusEntity
import io.plastique.users.UserEntity
import org.threeten.bp.ZonedDateTime

@Entity(tableName = "feed",
        foreignKeys = [
            ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["user_id"]),
            ForeignKey(entity = StatusEntity::class, parentColumns = ["id"], childColumns = ["status_id"]),
            ForeignKey(entity = FolderEntity::class, parentColumns = ["id"], childColumns = ["folder_id"])
        ],
        indices = [
            Index("user_id"),
            Index("status_id"),
            Index("folder_id"),
            Index("bucket_id", unique = true)
        ])
data class FeedElementEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "timestamp")
    val timestamp: ZonedDateTime,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "bucket_id")
    val bucketId: String? = null,

    @ColumnInfo(name = "bucket_total")
    val bucketTotal: Int = 0,

    @ColumnInfo(name = "folder_id")
    val folderId: String? = null,

    @ColumnInfo(name = "added_count")
    val addedCount: Int = 0,

    @ColumnInfo(name = "status_id")
    val statusId: String? = null,

    @ColumnInfo(name = "former_name")
    val formerName: String? = null
)
