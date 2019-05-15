package io.plastique.core.exceptions

import io.plastique.api.common.ErrorData
import io.plastique.core.client.HttpResponseCodes
import io.plastique.core.network.NoNetworkConnectionException
import java.io.IOException

open class HttpException(val responseCode: Int, message: String = "HTTP error $responseCode") : IOException(message)
class HttpTransportException(cause: Throwable) : IOException(cause)

class ApiException(responseCode: Int, val errorData: ErrorData) : HttpException(responseCode, "$responseCode ${errorData.type}: ${errorData.description}" +
        if (errorData.details.isNotEmpty()) "\n${errorData.details}" else "")

class RateLimitExceededException(responseCode: Int, requestPath: String) : HttpException(responseCode, "Rate limit exceeded for $requestPath")
class UserNotFoundException(val username: String, cause: Throwable) : Exception("User '$username' not found", cause)

val Throwable.isRetryable: Boolean
    get() = when (this) {
        is NoNetworkConnectionException,
        is HttpTransportException,
        is RateLimitExceededException -> true
        is HttpException -> responseCode >= HttpResponseCodes.INTERNAL_SERVER_ERROR
        else -> false
    }