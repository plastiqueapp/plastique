package io.plastique.inject.modules

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.webkit.CookieManager
import androidx.work.WorkManager
import dagger.Binds
import dagger.Lazy
import dagger.Module
import dagger.Provides
import io.plastique.BuildConfig
import io.plastique.R
import io.plastique.auth.SessionManagerImpl
import io.plastique.core.analytics.FirebaseTracker
import io.plastique.core.analytics.Tracker
import io.plastique.core.client.AccessTokenProvider
import io.plastique.core.client.ApiConfiguration
import io.plastique.core.config.AppConfig
import io.plastique.core.config.FirebaseAppConfig
import io.plastique.core.config.LocalAppConfig
import io.plastique.core.session.SessionManager
import io.plastique.deviations.list.LayoutMode
import io.plastique.users.UserRepository
import io.plastique.users.UserRepositoryImpl
import io.plastique.util.NetworkConnectivityMonitor
import io.plastique.util.NetworkConnectivityMonitorImpl
import io.plastique.util.Preferences
import javax.inject.Singleton

@Module
abstract class AppModule {
    @Binds
    abstract fun bindContext(application: Application): Context

    @Binds
    abstract fun bindSessionManager(impl: SessionManagerImpl): SessionManager

    @Binds
    abstract fun bindNetworkConnectivityMonitor(impl: NetworkConnectivityMonitorImpl): NetworkConnectivityMonitor

    @Binds
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Module
    companion object {
        @Provides
        @Singleton
        @JvmStatic
        fun provideSharedPreferences(context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        @Provides
        @Singleton
        @JvmStatic
        fun providePreferences(sharedPreferences: SharedPreferences): Preferences {
            return Preferences.Builder()
                    .sharedPreferences(sharedPreferences)
                    .addConverter(LayoutMode::class.java, LayoutMode.CONVERTER)
                    .build()
        }

        @Provides
        @JvmStatic
        fun provideCookieManager(): CookieManager = CookieManager.getInstance()

        @Provides
        @JvmStatic
        fun provideWorkManager(): WorkManager = WorkManager.getInstance()

        @Provides
        @Singleton
        @JvmStatic
        @Suppress("ConstantConditionIf")
        fun provideAppConfig(context: Context): AppConfig = if (BuildConfig.GOOGLE_SERVICES_ENABLED) {
            FirebaseAppConfig(R.xml.config_defaults)
        } else {
            LocalAppConfig(context, R.xml.config_defaults)
        }

        @Provides
        @JvmStatic
        @Suppress("ConstantConditionIf")
        fun provideAnalyticsTrackers(context: Context): List<Tracker> = if (BuildConfig.GOOGLE_SERVICES_ENABLED) {
            listOf(FirebaseTracker(context))
        } else {
            emptyList()
        }

        @Provides
        @Singleton
        @JvmStatic
        fun provideAccessTokenProvider(sessionManager: Lazy<SessionManager>): AccessTokenProvider = object : AccessTokenProvider {
            override fun getAccessToken(refresh: Boolean): String {
                return sessionManager.get().getAccessToken(refresh)
            }
        }

        @Provides
        @Singleton
        @JvmStatic
        fun provideApiConfiguration(context: Context): ApiConfiguration = ApiConfiguration(
                authUrl = "${context.packageName}://auth",
                clientId = context.getString(R.string.api_client_id),
                clientSecret = context.getString(R.string.api_client_secret),
                userAgent = "Plastique/android ${BuildConfig.VERSION_NAME}")
    }
}
