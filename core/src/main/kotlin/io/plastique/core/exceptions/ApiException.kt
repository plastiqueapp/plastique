package io.plastique.core.exceptions

import io.plastique.api.common.ErrorResponse
import retrofit2.Response
import java.io.IOException

open class ApiException : IOException {
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
}

open class ApiHttpException(response: Response<*>) : ApiException("HTTP error " + response.code()) {
    val responseCode: Int = response.code()
}

class ApiResponseException(response: Response<*>, val errorResponse: ErrorResponse) : ApiHttpException(response)
class ApiTransportException(cause: Throwable) : ApiException(cause)
class NoNetworkConnectionException : ApiException("Not connected to network")
