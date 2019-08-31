package io.plastique.deviations

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import io.plastique.core.cache.CacheEntryEntity

@Entity(
    tableName = "deviation_linkage",
    primaryKeys = ["key", "deviation_id"],
    foreignKeys = [
        ForeignKey(entity = CacheEntryEntity::class, parentColumns = ["key"], childColumns = ["key"]),
        ForeignKey(entity = DeviationEntity::class, parentColumns = ["id"], childColumns = ["deviation_id"])
    ],
    indices = [
        Index("deviation_id")
    ])
data class DeviationLinkage(
    @ColumnInfo(name = "key")
    val key: String,

    @ColumnInfo(name = "deviation_id")
    val deviationId: String,

    @ColumnInfo(name = "order")
    val order: Int
)
