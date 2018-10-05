package io.plastique.api.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ErrorResponse(
    @Json(name = "error")
    val errorType: ErrorType,

    @Json(name = "error_code")
    val errorCode: Int = -1,

    @Json(name = "error_description")
    val errorDescription: String? = null,

    @Json(name = "error_details")
    val errorDetails: Map<String, String> = emptyMap()
)
