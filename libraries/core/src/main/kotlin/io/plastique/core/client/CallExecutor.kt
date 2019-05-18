package io.plastique.core.client

import io.plastique.core.exceptions.HttpTransportException
import io.plastique.core.network.NetworkConnectivityChecker
import io.plastique.core.network.NoNetworkConnectionException
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class CallExecutor @Inject constructor(
    private val errorResponseParser: ErrorResponseParser,
    private val networkConnectivityChecker: NetworkConnectivityChecker
) {
    fun <T> execute(call: Call<T>): Response<T> {
        if (!networkConnectivityChecker.isConnectedToNetwork) {
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
