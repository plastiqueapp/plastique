package io.plastique.api.watch

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WatcherList(
    @Json(name = "has_more")
    val hasMore: Boolean,

    @Json(name = "next_offset")
    val nextOffset: Int? = 0,

    @Json(name = "results")
    val watchers: List<Watcher> = emptyList()
)
