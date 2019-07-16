package io.plastique.inject.modules

import dagger.Binds
import dagger.Module
import dagger.Provides
import io.plastique.core.client.OkHttpClientFactory
import io.plastique.core.network.NetworkConnectivityChecker
import io.plastique.core.network.NetworkConnectivityCheckerImpl
import io.plastique.core.network.NetworkConnectivityMonitor
import io.plastique.core.network.NetworkConnectivityMonitorImpl
import okhttp3.OkHttpClient
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
        fun provideOkHttpClient(factory: OkHttpClientFactory): OkHttpClient {
            return factory.createClient()
        }
    }
}
