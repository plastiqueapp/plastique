package io.plastique.api.deviations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeviationMetadataList(
    @Json(name = "metadata")
    val metadata: List<DeviationMetadataDto>
)
