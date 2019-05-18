package io.plastique.inject.modules

import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import javax.inject.Named

@Module
object OkHttpInterceptorModule {
    @Provides
    @JvmStatic
    fun provideInterceptors(): List<Interceptor> = emptyList()

    @Provides
    @Named("network")
    @JvmStatic
    fun provideNetworkInterceptors(): List<Interceptor> = emptyList()
}
