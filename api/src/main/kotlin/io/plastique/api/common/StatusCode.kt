package io.plastique.api.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
enum class StatusCode {
    @Json(name = "success")
    Success,

    @Json(name = "error")
    Error
}
