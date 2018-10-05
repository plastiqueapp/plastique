package io.plastique.api.deviations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeviationMetadataResponse(
    @Json(name = "metadata")
    val metadata: List<DeviationMetadata>
)
