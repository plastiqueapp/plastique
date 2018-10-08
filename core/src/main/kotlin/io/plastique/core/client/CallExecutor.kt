package io.plastique.core.client

import io.plastique.core.exceptions.ApiException
import io.plastique.core.exceptions.ApiTransportException
import io.plastique.core.exceptions.NoNetworkConnectionException
import io.plastique.util.NetworkConnectivityMonitor
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class CallExecutor @Inject constructor(
    private val connectivityMonitor: NetworkConnectivityMonitor,
    private val errorResponseParser: ErrorResponseParser
) {
    @Throws(ApiException::class)
    fun <T> execute(call: Call<T>): Response<T> {
        if (!connectivityMonitor.isConnectedToNetwork) {
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
