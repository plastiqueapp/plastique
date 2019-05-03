package io.plastique.inject.modules

import com.facebook.flipper.core.FlipperClient
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.stetho.okhttp3.StethoInterceptor
import dagger.Module
import dagger.Provides
import io.plastique.core.network.ConnectivityCheckingInterceptor
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Named

@Module
object DebugOkHttpInterceptorModule {
    @Provides
    @JvmStatic
    fun provideInterceptors(): List<Interceptor> =
            listOf(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))

    @Provides
    @JvmStatic
    @Named("network")
    fun provideNetworkInterceptors(connectivityCheckingInterceptor: ConnectivityCheckingInterceptor, flipperClient: FlipperClient): List<Interceptor> =
            listOf(connectivityCheckingInterceptor,
                    StethoInterceptor(),
                    FlipperOkhttpInterceptor(flipperClient.getPlugin(NetworkFlipperPlugin.ID)))
}
