package io.plastique.api.deviations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeviationList(
    @Json(name = "has_more")
    val hasMore: Boolean = false,

    @Json(name = "next_offset")
    val nextOffset: Int? = 0,

    @Json(name = "results")
    val deviations: List<Deviation> = emptyList()
)
