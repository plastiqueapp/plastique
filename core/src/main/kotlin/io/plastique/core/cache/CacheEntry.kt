package io.plastique.core.cache

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.Duration
import org.threeten.bp.Instant

@Entity(tableName = "cache_entries")
data class CacheEntry(
    @PrimaryKey
    @ColumnInfo(name = "key")
    val key: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Instant,

    @ColumnInfo(name = "metadata")
    val metadata: String? = null
) {

    fun isActual(now: Instant, cacheDuration: Duration): Boolean {
        return timestamp.plus(cacheDuration).isAfter(now) && timestamp.isBefore(now)
    }
}
