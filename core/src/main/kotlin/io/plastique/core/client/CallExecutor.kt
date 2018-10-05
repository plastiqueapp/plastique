package io.plastique.core.client

import io.plastique.core.exceptions.ApiException
import io.plastique.core.exceptions.ApiTransportException
import io.plastique.core.exceptions.NoNetworkConnectionException
import io.plastique.util.NetworkConnectivityChecker
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class CallExecutor @Inject constructor(
    private val errorResponseParser: ErrorResponseParser,
    private val networkConnectivityChecker: NetworkConnectivityChecker
) {
    @Throws(ApiException::class)
    fun <T> execute(call: Call<T>): Response<T> {
        if (!networkConnectivityChecker.isConnectedToNetwork()) {
            throw NoNetworkConnectionException()
        }
        val response = try {
            call.execute()
        } catch (e: IOException) {
            throw ApiTransportException(e)
        }

        if (!response.isSuccessful) {
            throw errorResponseParser.parse(response)
        }
        return response
    }
}
