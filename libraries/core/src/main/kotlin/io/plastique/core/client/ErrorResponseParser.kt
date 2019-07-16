package io.plastique.core.client

import retrofit2.Response

interface ErrorResponseParser {
    fun parse(response: Response<*>): HttpException
}
