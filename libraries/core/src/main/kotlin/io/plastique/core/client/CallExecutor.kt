package io.plastique.core.client

import io.plastique.core.exceptions.HttpTransportException
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class CallExecutor @Inject constructor(
    private val errorResponseParser: ErrorResponseParser
) {
    fun <T> execute(call: Call<T>): Response<T> {
        val response = try {
            call.execute()
        } catch (e: IOException) {
            throw HttpTransportException(e)
        }

        if (!response.isSuccessful) {
            throw errorResponseParser.parse(response)
        }
        return response
    }
}
