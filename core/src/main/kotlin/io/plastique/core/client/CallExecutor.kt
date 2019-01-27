package io.plastique.core.client

import io.plastique.core.exceptions.HttpTransportException
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
    fun <T> execute(call: Call<T>): Response<T> {
        if (!connectivityMonitor.isConnectedToNetwork) {
            throw NoNetworkConnectionException()
        }
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
