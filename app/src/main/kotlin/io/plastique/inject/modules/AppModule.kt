package io.plastique.inject.modules

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.webkit.CookieManager
import androidx.work.WorkManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import io.plastique.BuildConfig
import io.plastique.R
import io.plastique.auth.SessionManagerImpl
import io.plastique.collections.FavoritesModel
import io.plastique.collections.FavoritesModelImpl
import io.plastique.core.analytics.FirebaseTracker
import io.plastique.core.analytics.Tracker
import io.plastique.core.browser.CookieCleaner
import io.plastique.core.config.AppConfig
import io.plastique.core.config.FirebaseAppConfig
import io.plastique.core.config.LocalAppConfig
import io.plastique.core.session.OnLogoutListener
import io.plastique.core.session.SessionManager
import io.plastique.core.themes.ThemeIdConverter
import io.plastique.core.work.WorkerCleaner
import io.plastique.deviations.list.LayoutModeConverter
import io.plastique.main.MainFragmentFactory
import io.plastique.main.MainFragmentFactoryImpl
import io.plastique.users.UserProfilePageProviderImpl
import io.plastique.users.profile.UserProfilePageProvider
import io.plastique.util.Cryptor
import io.plastique.util.Preferences
import io.plastique.util.SystemTimeProvider
import io.plastique.util.TimeProvider
import io.plastique.watch.WatchManager
import io.plastique.watch.WatchManagerImpl
import javax.inject.Singleton

@Module(includes = [
    CacheModule::class,
    RepositoryModule::class,
    WorkerModule::class
])
abstract class AppModule {
    @Binds
    abstract fun bindContext(application: Application): Context

    @Binds
    abstract fun bindSessionManager(impl: SessionManagerImpl): SessionManager

    @Binds
    abstract fun bindMainFragmentFactory(impl: MainFragmentFactoryImpl): MainFragmentFactory

    @Binds
    abstract fun bindUserProfilePageProvider(impl: UserProfilePageProviderImpl): UserProfilePageProvider

    @Binds
    abstract fun bindFavoritesModel(impl: FavoritesModelImpl): FavoritesModel

    @Binds
    abstract fun bindWatchManager(impl: WatchManagerImpl): WatchManager

    @Binds
    @IntoSet
    abstract fun bindCookieCleaner(impl: CookieCleaner): OnLogoutListener

    @Binds
    @IntoSet
    abstract fun bindWorkerCleaner(impl: WorkerCleaner): OnLogoutListener

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideTimeProvider(): TimeProvider = SystemTimeProvider

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
            FirebaseAppConfig(R.xml.config_defaults)
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
        fun provideCryptor(): Cryptor = Cryptor.create()
    }
}
