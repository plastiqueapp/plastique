package io.plastique.api.users

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StatusList(
    @Json(name = "has_more")
    val hasMore: Boolean = false,

    @Json(name = "next_offset")
    val nextOffset: Int? = 0,

    @Json(name = "results")
    val statuses: List<Status> = emptyList()
)
