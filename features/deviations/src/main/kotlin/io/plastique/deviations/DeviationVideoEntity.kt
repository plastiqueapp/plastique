package io.plastique.deviations

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import org.threeten.bp.Duration

@Entity(
    tableName = "deviation_videos",
    primaryKeys = ["deviation_id", "quality"],
    foreignKeys = [
        ForeignKey(entity = DeviationEntity::class, parentColumns = ["id"], childColumns = ["deviation_id"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [
        Index("deviation_id")
    ])
data class DeviationVideoEntity(
    @ColumnInfo(name = "deviation_id")
    val deviationId: String,

    @ColumnInfo(name = "quality")
    val quality: String,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "duration")
    val duration: Duration,

    @ColumnInfo(name = "file_size")
    val fileSize: Int
)
