package io.plastique.statuses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import io.plastique.api.common.StringEnum
import io.plastique.deviations.DeviationEntity
import io.plastique.users.UserEntity
import org.threeten.bp.ZonedDateTime

@Entity(tableName = "statuses",
        foreignKeys = [
            ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["author_id"]),
            ForeignKey(entity = DeviationEntity::class, parentColumns = ["id"], childColumns = ["shared_deviation_id"], onDelete = ForeignKey.SET_NULL),
            ForeignKey(entity = StatusEntity::class, parentColumns = ["id"], childColumns = ["shared_status_id"], onDelete = ForeignKey.SET_NULL)
        ],
        indices = [
            Index("author_id"),
            Index("shared_deviation_id"),
            Index("shared_status_id")
        ])
@TypeConverters(ShareTypeConverter::class)
data class StatusEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "body")
    val body: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: ZonedDateTime,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "author_id")
    val authorId: String,

    @ColumnInfo(name = "comment_count")
    val commentCount: Int,

    @ColumnInfo(name = "share_type")
    val shareType: ShareType,

    @ColumnInfo(name = "shared_deviation_id")
    val sharedDeviationId: String?,

    @ColumnInfo(name = "shared_status_id")
    val sharedStatusId: String?
)

enum class ShareType(override val value: String) : StringEnum {
    None("none"),
    Deviation("deviation"),
    Status("status")
}

class ShareTypeConverter {
    @TypeConverter
    fun fromString(value: String): ShareType {
        return ShareType.values().first { it.value == value }
    }

    @TypeConverter
    fun toString(shareType: ShareType): String {
        return shareType.value
    }
}
