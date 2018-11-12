package io.plastique.api.deviations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeviationMetadataDto(
    @Json(name = "deviationid")
    val deviationId: String,

    @Json(name = "description")
    val description: String? = null
)
