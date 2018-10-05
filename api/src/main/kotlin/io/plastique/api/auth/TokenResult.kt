package io.plastique.api.auth

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.common.StatusCode

@JsonClass(generateAdapter = true)
data class TokenResult(
    @Json(name = "access_token")
    val accessToken: String,

    @Json(name = "refresh_token")
    val refreshToken: String? = null,

    @Json(name = "expires_in")
    val expiresIn: Int,

    @Json(name = "status")
    val status: StatusCode
)
