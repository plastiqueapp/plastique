package io.plastique.api.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PagedListResult<T : Any>(
    @Json(name = "has_more")
    val hasMore: Boolean = false,

    @Json(name = "next_offset")
    val nextOffset: Int? = 0,

    @Json(name = "results")
    val results: List<T> = emptyList()
)
