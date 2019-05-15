package io.plastique.api.deviations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DownloadInfoDto(
    @Json(name = "src")
    val url: String,

    @Json(name = "width")
    val width: Int,

    @Json(name = "height")
    val height: Int,

    @Json(name = "filesize")
    val fileSize: Int
)
