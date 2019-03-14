package io.plastique.api.deviations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeviationContentDto(
    @Json(name = "html")
    val html: String,

    @Json(name = "css")
    val css: String?,

    @Json(name = "css_fonts")
    val cssFonts: List<String> = emptyList()
)
