package io.plastique.api.feed

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeedResult(
    @Json(name = "cursor")
    val cursor: String?,

    @Json(name = "has_more")
    val hasMore: Boolean,

    @Json(name = "items")
    val items: List<FeedElement?> = emptyList()
)
