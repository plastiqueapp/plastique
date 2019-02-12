package io.plastique.deviations.download

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import io.plastique.api.deviations.DownloadInfoDto
import io.plastique.deviations.DeviationEntity
import io.plastique.util.Size

@Entity(tableName = "deviation_download",
        foreignKeys = [
            ForeignKey(entity = DeviationEntity::class, parentColumns = ["id"], childColumns = ["deviation_id"], onDelete = ForeignKey.CASCADE)
        ])
data class DownloadInfoEntity(
    @PrimaryKey
    @ColumnInfo(name = "deviation_id")
    val deviationId: String,

    @ColumnInfo(name = "url")
    val downloadUrl: String,

    @ColumnInfo(name = "size")
    val size: Size,

    @ColumnInfo(name = "file_size")
    val fileSize: Int
)
