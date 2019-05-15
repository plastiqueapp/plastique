package io.plastique.core.client

import io.plastique.core.exceptions.HttpException
import retrofit2.Response

interface ErrorResponseParser {
    fun parse(response: Response<*>): HttpException
}
