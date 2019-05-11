package io.plastique.watch

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import io.plastique.core.cache.CacheEntry
import io.plastique.users.UserEntity

@Entity(
    tableName = "watchers",
    primaryKeys = ["key", "user_id"],
    foreignKeys = [
        ForeignKey(entity = CacheEntry::class, parentColumns = ["key"], childColumns = ["key"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["user_id"])
    ],
    indices = [
        Index("user_id")
    ])
data class WatcherEntity(
    @ColumnInfo(name = "key")
    val key: String,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "order")
    val order: Int
)
