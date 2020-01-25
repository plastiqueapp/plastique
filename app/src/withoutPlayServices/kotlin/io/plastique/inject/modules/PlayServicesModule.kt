package io.plastique.inject.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import io.plastique.R
import io.plastique.core.config.AppConfig
import io.plastique.core.config.LocalAppConfig
import javax.inject.Singleton

@Module
object PlayServicesModule {
    @Provides
    @Singleton
    fun provideAppConfig(context: Context): AppConfig = LocalAppConfig(context, R.xml.config_defaults)
}
