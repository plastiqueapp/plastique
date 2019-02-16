package io.plastique.inject.modules

import com.facebook.flipper.core.FlipperClient
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.stetho.okhttp3.StethoInterceptor
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
object DebugOkHttpModule {
    @Provides
    @Singleton
    @JvmStatic
    fun provideOkHttpClient(flipperClient: FlipperClient): OkHttpClient {
        return OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .addNetworkInterceptor(StethoInterceptor())
                .addNetworkInterceptor(FlipperOkhttpInterceptor(flipperClient.getPlugin(NetworkFlipperPlugin.ID)))
                .build()
    }
}
