package io.plastique.core.exceptions

import io.plastique.api.common.ErrorData
import java.io.IOException

open class HttpException(responseCode: Int, message: String = "HTTP error $responseCode") : IOException(message)
class HttpTransportException(cause: Throwable) : IOException(cause)
class NoNetworkConnectionException : IOException("Not connected to network")

class ApiException(responseCode: Int, val errorData: ErrorData) : HttpException(responseCode, "$responseCode ${errorData.type}: ${errorData.description}" +
        if (errorData.details.isNotEmpty()) "\n${errorData.details}" else "")

class UserNotFoundException(val username: String, cause: Throwable)
    : Exception("User $username not found", cause)
