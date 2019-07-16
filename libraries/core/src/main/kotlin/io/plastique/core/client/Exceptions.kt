package io.plastique.core.client

import java.io.IOException

open class HttpException(val responseCode: Int, message: String = "HTTP error $responseCode") : IOException(message)
class HttpTransportException(cause: Throwable) : IOException(cause)

class RateLimitExceededException(responseCode: Int, requestPath: String) : HttpException(responseCode, "Rate limit exceeded for $requestPath")

class NoNetworkConnectionException : IOException("Not connected to any network")
