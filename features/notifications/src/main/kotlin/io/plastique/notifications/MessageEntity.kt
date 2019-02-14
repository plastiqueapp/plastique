package io.plastique.notifications

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.plastique.collections.FolderEntity
import io.plastique.comments.CommentEntity
import io.plastique.deviations.DeviationEntity
import io.plastique.users.UserEntity
import org.threeten.bp.ZonedDateTime

@Entity(tableName = "messages",
        foreignKeys = [
            ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["originator_id"]),
            ForeignKey(entity = DeviationEntity::class, parentColumns = ["id"], childColumns = ["deviation_id"]),
            ForeignKey(entity = CommentEntity::class, parentColumns = ["id"], childColumns = ["comment_id"]),
            ForeignKey(entity = FolderEntity::class, parentColumns = ["id"], childColumns = ["collection_folder_id"]),
            ForeignKey(entity = DeviationEntity::class, parentColumns = ["id"], childColumns = ["subject_deviation_id"]),
            ForeignKey(entity = CommentEntity::class, parentColumns = ["id"], childColumns = ["subject_comment_id"]),
            ForeignKey(entity = FolderEntity::class, parentColumns = ["id"], childColumns = ["subject_collection_folder_id"])
        ],
        indices = [
            Index("collection_folder_id"),
            Index("comment_id"),
            Index("deviation_id"),
            Index("originator_id"),
            Index("subject_comment_id"),
            Index("subject_deviation_id"),
            Index("subject_collection_folder_id")
        ])
data class MessageEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "time")
    val time: ZonedDateTime,

    @ColumnInfo(name = "originator_id")
    val originatorId: String,

    @ColumnInfo(name = "deviation_id")
    val deviationId: String?,

    @ColumnInfo(name = "comment_id")
    val commentId: String?,

    @ColumnInfo(name = "collection_folder_id")
    val collectionFolderId: String?,

    @Embedded(prefix = "subject_")
    val subject: MessageSubjectEntity?
)

data class MessageSubjectEntity(
    @ColumnInfo(name = "deviation_id")
    val deviationId: String?,

    @ColumnInfo(name = "comment_id")
    val commentId: String?,

    @ColumnInfo(name = "collection_folder_id")
    val collectionFolderId: String?
)
