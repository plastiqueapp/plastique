package io.plastique.api.feed

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeedElementList(
    @Json(name = "has_more")
    val hasMore: Boolean,

    @Json(name = "cursor")
    val cursor: String? = null,

    @Json(name = "items")
    val items: List<FeedElementDto?> = emptyList()
)
