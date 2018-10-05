package io.plastique.api.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
enum class ErrorType {
    @Json(name = "invalid_client")
    InvalidClient,

    @Json(name = "invalid_token")
    InvalidToken,

    @Json(name = "invalid_grant")
    InvalidGrantType,

    @Json(name = "invalid_request")
    InvalidRequest,

    @Json(name = "insufficient_scope")
    InsufficientScope,

    @Json(name = "unauthorized")
    Unauthorized,

    @Json(name = "unauthorized_client")
    UnauthorizedClient,

    @Json(name = "unverified_account")
    UnverifiedAccount,

    @Json(name = "server_error")
    ServerError,

    @Json(name = "version_error")
    VersionError
}
