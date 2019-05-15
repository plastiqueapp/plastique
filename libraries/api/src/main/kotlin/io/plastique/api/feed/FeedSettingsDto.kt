package io.plastique.api.feed

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeedSettingsDto(
    @Json(name = "include")
    val include: Map<String, Boolean>
)
