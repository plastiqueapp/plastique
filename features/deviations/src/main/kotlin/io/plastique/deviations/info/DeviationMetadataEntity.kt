package io.plastique.deviations.info

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import io.plastique.api.deviations.DeviationMetadataDto
import io.plastique.core.converters.StringListConverter
import io.plastique.deviations.DeviationEntity

@Entity(tableName = "deviation_metadata",
        foreignKeys = [
            ForeignKey(entity = DeviationEntity::class, parentColumns = ["id"], childColumns = ["deviation_id"], onDelete = ForeignKey.CASCADE)
        ])
@TypeConverters(StringListConverter::class)
data class DeviationMetadataEntity(
    @PrimaryKey
    @ColumnInfo(name = "deviation_id")
    val deviationId: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "tags")
    val tags: List<String>
)
