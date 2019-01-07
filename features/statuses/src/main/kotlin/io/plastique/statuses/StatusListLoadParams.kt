package io.plastique.statuses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StatusListLoadParams(
    @Json(name = "username")
    val username: String,

    @Json(name = "show_mature")
    val matureContent: Boolean
)
