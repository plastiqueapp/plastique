package io.plastique.feed

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import io.plastique.deviations.DeviationEntity

@Entity(
    tableName = "feed_deviations",
    primaryKeys = ["feed_element_id", "deviation_id"],
    foreignKeys = [
        ForeignKey(entity = FeedElementEntity::class, parentColumns = ["id"], childColumns = ["feed_element_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = DeviationEntity::class, parentColumns = ["id"], childColumns = ["deviation_id"])
    ],
    indices = [
        Index("deviation_id")
    ])
data class FeedElementDeviation(
    @ColumnInfo(name = "feed_element_id")
    val feedElementId: Long,

    @ColumnInfo(name = "deviation_id")
    val deviationId: String,

    @ColumnInfo(name = "order")
    val order: Int
)
