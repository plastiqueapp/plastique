package io.plastique.core.exceptions

import io.plastique.api.common.ErrorData
import java.io.IOException

open class ApiException : IOException {
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
}

open class ApiHttpException(responseCode: Int) : ApiException("HTTP error $responseCode")
class ApiResponseException(val errorData: ErrorData) : ApiException("${errorData.type}: ${errorData.description}" +
        if (errorData.details.isNotEmpty()) "\n${errorData.details}" else "")

class ApiTransportException(cause: Throwable) : ApiException(cause)
class NoNetworkConnectionException : ApiException("Not connected to network")
