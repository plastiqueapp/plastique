package io.plastique.core.exceptions

import io.plastique.core.client.HttpResponseCodes
import io.plastique.core.network.NoNetworkConnectionException
import java.io.IOException

open class HttpException(val responseCode: Int, message: String = "HTTP error $responseCode") : IOException(message)
class HttpTransportException(cause: Throwable) : IOException(cause)

class RateLimitExceededException(responseCode: Int, requestPath: String) : HttpException(responseCode, "Rate limit exceeded for $requestPath")

val Throwable.isRetryable: Boolean
    get() = when (this) {
        is NoNetworkConnectionException,
        is HttpTransportException,
        is RateLimitExceededException -> true
        is HttpException -> responseCode >= HttpResponseCodes.INTERNAL_SERVER_ERROR
        else -> false
    }
