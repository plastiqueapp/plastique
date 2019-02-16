package io.plastique.users

import androidx.room.ColumnInfo

data class WatchInfoEntity(
    @ColumnInfo(name = "is_watching")
    val isWatching: Boolean,

    @ColumnInfo(name = "stats_watchers")
    val watcherCount: Int
)
