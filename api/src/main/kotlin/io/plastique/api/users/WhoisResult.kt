package io.plastique.api.users

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WhoisResult(
    @Json(name = "results")
    val users: List<User>
)
