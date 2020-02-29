package io.plastique.inject.modules

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import io.plastique.BuildConfig
import io.plastique.R
import io.plastique.core.analytics.FirebaseTracker
import io.plastique.core.analytics.Tracker
import io.plastique.core.config.AppConfig
import io.plastique.core.config.FirebaseAppConfig
import io.plastique.core.crash.CrashlyticsInitializer
import io.plastique.core.crash.CrashlyticsTree
import io.plastique.core.init.Initializer
import org.threeten.bp.Duration
import timber.log.Timber
import javax.inject.Singleton

@Module
abstract class PlayServicesModule {
    @Binds
    @IntoSet
    abstract fun bindCrashlyticsTree(crashlyticsTree: CrashlyticsTree): Timber.Tree

    @Binds
    @IntoSet
    abstract fun bindFirebaseTracker(firebaseTracker: FirebaseTracker): Tracker

    companion object {
        @Provides
        @Singleton
        fun provideAppConfig(): AppConfig = FirebaseAppConfig(R.xml.config_defaults, CONFIG_FETCH_INTERVAL)

        @Provides
        @IntoSet
        fun provideCrashlyticsInitializer(): Initializer = CrashlyticsInitializer(BuildConfig.DEBUG)

        private val CONFIG_FETCH_INTERVAL = if (BuildConfig.DEBUG) {
            Duration.ofMinutes(2)
        } else {
            Duration.ofHours(6)
        }
    }
}
