package io.plastique.api.watch

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IsWatchingResult(
    @Json(name = "watching")
    val isWatching: Boolean
)
