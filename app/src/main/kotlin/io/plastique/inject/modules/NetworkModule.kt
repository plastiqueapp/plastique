package io.plastique.inject.modules

import dagger.Binds
import dagger.Module
import dagger.Provides
import io.plastique.core.network.NetworkConnectivityChecker
import io.plastique.core.network.NetworkConnectivityCheckerImpl
import io.plastique.core.network.NetworkConnectivityMonitor
import io.plastique.core.network.NetworkConnectivityMonitorImpl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
abstract class NetworkModule {
    @Binds
    abstract fun bindNetworkConnectivityChecker(impl: NetworkConnectivityCheckerImpl): NetworkConnectivityChecker

    @Binds
    abstract fun bindNetworkConnectivityMonitor(impl: NetworkConnectivityMonitorImpl): NetworkConnectivityMonitor

    @Module
    companion object {
        @Provides
        @Singleton
        @JvmStatic
        fun provideOkHttpClient(
            interceptors: List<@JvmSuppressWildcards Interceptor>,
            @Named("network") networkInterceptors: List<@JvmSuppressWildcards Interceptor>
        ): OkHttpClient {
            return OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .apply {
                        interceptors().addAll(interceptors)
                        networkInterceptors().addAll(networkInterceptors)
                    }
                    .build()
        }
    }
}
