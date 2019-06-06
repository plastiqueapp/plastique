package io.plastique.api.deviations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideoDto(
    @Json(name = "src")
    val url: String,

    @Json(name = "quality")
    val quality: String,

    @Json(name = "duration")
    val duration: Int,

    @Json(name = "filesize")
    val fileSize: Int
)
