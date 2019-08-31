package io.plastique.statuses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import io.plastique.core.cache.CacheEntryEntity

@Entity(
    tableName = "user_statuses",
    primaryKeys = ["key", "status_id"],
    foreignKeys = [
        ForeignKey(entity = CacheEntryEntity::class, parentColumns = ["key"], childColumns = ["key"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = StatusEntity::class, parentColumns = ["id"], childColumns = ["status_id"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [
        Index("status_id")
    ])
data class StatusLinkage(
    @ColumnInfo(name = "key")
    val key: String,

    @ColumnInfo(name = "status_id")
    val statusId: String,

    @ColumnInfo(name = "order")
    val order: Int
)
