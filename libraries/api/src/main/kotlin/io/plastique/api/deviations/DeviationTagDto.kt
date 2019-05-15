package io.plastique.api.deviations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeviationTagDto(
    @Json(name = "tag_name")
    val name: String
)
