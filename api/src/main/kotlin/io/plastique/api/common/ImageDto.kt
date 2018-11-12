package io.plastique.api.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ImageDto(
    @Json(name = "width")
    val width: Int,

    @Json(name = "height")
    val height: Int,

    @Json(name = "src")
    val url: String
)
