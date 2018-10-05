package io.plastique.settings.about.licenses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class License(
    @Json(name = "name")
    val libraryName: String,

    @Json(name = "description")
    val libraryDescription: String? = null,

    @Json(name = "license")
    val license: String,

    @Json(name = "url")
    val url: String
)
