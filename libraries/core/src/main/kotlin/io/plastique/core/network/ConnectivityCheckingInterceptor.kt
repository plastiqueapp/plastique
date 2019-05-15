package io.plastique.core.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class ConnectivityCheckingInterceptor @Inject constructor(
    private val networkConnectivityChecker: NetworkConnectivityChecker
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!networkConnectivityChecker.isConnectedToNetwork) {
            throw NoNetworkConnectionException()
        }
        return chain.proceed(chain.request())
    }
}
