package io.plastique.inject.modules

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.webkit.CookieManager
import androidx.preference.PreferenceManager
import androidx.work.WorkManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.plastique.BuildConfig
import io.plastique.R
import io.plastique.auth.SessionManagerImpl
import io.plastique.collections.FavoritesModel
import io.plastique.collections.FavoritesModelImpl
import io.plastique.core.analytics.FirebaseTracker
import io.plastique.core.analytics.Tracker
import io.plastique.core.config.AppConfig
import io.plastique.core.config.FirebaseAppConfig
import io.plastique.core.config.LocalAppConfig
import io.plastique.core.session.SessionManager
import io.plastique.core.themes.ThemeIdConverter
import io.plastique.deviations.list.LayoutModeConverter
import io.plastique.main.MainPageProvider
import io.plastique.main.MainPageProviderImpl
import io.plastique.users.UserProfilePageProviderImpl
import io.plastique.users.profile.UserProfilePageProvider
import io.plastique.util.AesCryptor
import io.plastique.util.Cryptor
import io.plastique.util.InstantAppHelper
import io.plastique.util.NoCryptor
import io.plastique.util.Preferences
import io.plastique.watch.WatchManager
import io.plastique.watch.WatchManagerImpl
import org.threeten.bp.Clock
import org.threeten.bp.Duration
import javax.inject.Singleton

@Module(includes = [
    RepositoryModule::class,
    SessionModule::class,
    WorkerModule::class
])
abstract class AppModule {
    @Binds
    abstract fun bindContext(application: Application): Context

    @Binds
    abstract fun bindSessionManager(impl: SessionManagerImpl): SessionManager

    @Binds
    abstract fun bindMainPageProvider(impl: MainPageProviderImpl): MainPageProvider

    @Binds
    abstract fun bindUserProfilePageProvider(impl: UserProfilePageProviderImpl): UserProfilePageProvider

    @Binds
    abstract fun bindFavoritesModel(impl: FavoritesModelImpl): FavoritesModel

    @Binds
    abstract fun bindWatchManager(impl: WatchManagerImpl): WatchManager

    @Module
    companion object {
        private val CONFIG_FETCH_INTERVAL = if (BuildConfig.DEBUG) {
            Duration.ofMinutes(2)
        } else {
            Duration.ofHours(6)
        }

        @Provides
        @JvmStatic
        fun provideClock(): Clock = Clock.systemDefaultZone()

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
                .addConverter(LayoutModeConverter)
                .addConverter(ThemeIdConverter)
                .build()
        }

        @Provides
        @JvmStatic
        fun provideCookieManager(): CookieManager = CookieManager.getInstance()

        @Provides
        @JvmStatic
        fun provideWorkManager(context: Context): WorkManager = WorkManager.getInstance(context)

        @Provides
        @Singleton
        @JvmStatic
        fun provideAppConfig(context: Context): AppConfig = if (BuildConfig.GOOGLE_SERVICES_ENABLED) {
            FirebaseAppConfig(R.xml.config_defaults, CONFIG_FETCH_INTERVAL)
        } else {
            LocalAppConfig(context, R.xml.config_defaults)
        }

        @Provides
        @JvmStatic
        fun provideAnalyticsTrackers(context: Context): List<Tracker> = if (BuildConfig.GOOGLE_SERVICES_ENABLED) {
            listOf(FirebaseTracker(context))
        } else {
            emptyList()
        }

        @Provides
        @JvmStatic
        fun provideCryptor(instantAppHelper: InstantAppHelper): Cryptor {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !instantAppHelper.isInstantApp) {
                AesCryptor()
            } else {
                NoCryptor()
            }
        }
    }
}
