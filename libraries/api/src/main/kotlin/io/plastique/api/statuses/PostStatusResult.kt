package io.plastique.api.statuses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PostStatusResult(
    @Json(name = "statusid")
    val statusId: String
)
