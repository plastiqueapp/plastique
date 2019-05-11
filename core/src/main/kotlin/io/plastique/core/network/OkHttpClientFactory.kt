package io.plastique.core.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class OkHttpClientFactory @Inject constructor(
    private val interceptors: List<@JvmSuppressWildcards Interceptor>,
    @Named("network") private val networkInterceptors: List<@JvmSuppressWildcards Interceptor>
) {
    fun createClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
            .writeTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
            .apply {
                interceptors().addAll(interceptors)
                networkInterceptors().addAll(networkInterceptors)
            }
            .build()
    }

    companion object {
        private val CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(20)
    }
}
