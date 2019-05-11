package io.plastique.deviations

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import io.plastique.api.common.StringEnum
import io.plastique.util.Size

@Entity(
    tableName = "deviation_images",
    foreignKeys = [
        ForeignKey(entity = DeviationEntity::class, parentColumns = ["id"], childColumns = ["deviation_id"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [
        Index("deviation_id")
    ])
@TypeConverters(DeviationImageTypeConverter::class)
data class DeviationImageEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "deviation_id")
    val deviationId: String,

    @ColumnInfo(name = "image_type")
    val type: DeviationImageType,

    @ColumnInfo(name = "size")
    val size: Size,

    @ColumnInfo(name = "url")
    val url: String
)

enum class DeviationImageType(override val value: String) : StringEnum {
    Content("content"),
    Preview("preview"),
    Thumbnail("thumbnail")
}

class DeviationImageTypeConverter {
    @TypeConverter
    fun fromString(value: String): DeviationImageType {
        return DeviationImageType.values().first { it.value == value }
    }

    @TypeConverter
    fun toString(imageType: DeviationImageType): String {
        return imageType.value
    }
}
