package io.plastique.api.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ListResult<T : Any>(
    @Json(name = "results")
    val results: List<T> = emptyList()
)
