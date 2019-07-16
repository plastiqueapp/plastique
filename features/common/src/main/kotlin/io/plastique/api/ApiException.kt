package io.plastique.api

import io.plastique.api.common.ErrorData
import io.plastique.core.client.HttpException

class ApiException(responseCode: Int, val errorData: ErrorData) : HttpException(responseCode, "$responseCode ${errorData.type}: ${errorData.description}" +
        if (errorData.details.isNotEmpty()) "\n${errorData.details}" else "")
