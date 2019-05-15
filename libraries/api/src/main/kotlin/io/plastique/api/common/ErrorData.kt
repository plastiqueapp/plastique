package io.plastique.api.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ErrorData(
    @Json(name = "error")
    val type: ErrorType,

    @Json(name = "error_code")
    val code: Int = -1,

    @Json(name = "error_description")
    val description: String = "",

    @Json(name = "error_details")
    val details: Map<String, String> = emptyMap()
)
