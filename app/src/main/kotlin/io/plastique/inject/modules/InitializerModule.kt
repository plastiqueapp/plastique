package io.plastique.inject.modules

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet
import dagger.multibindings.IntoSet
import io.plastique.BuildConfig
import io.plastique.core.analytics.AnalyticsInitializer
import io.plastique.core.init.AndroidThreeTenInitializer
import io.plastique.core.init.Initializer
import io.plastique.core.init.RxJavaInitializer
import io.plastique.core.reporting.FabricInitializer
import io.plastique.core.work.WorkManagerInitializer
import javax.inject.Provider

@Module
abstract class InitializerModule {
    @Binds
    @IntoSet
    abstract fun bindAnalyticsInitializer(impl: AnalyticsInitializer): Initializer

    @Binds
    @IntoSet
    abstract fun bindAndroidThreeTenInitializer(impl: AndroidThreeTenInitializer): Initializer

    @Binds
    @IntoSet
    abstract fun bindRxJavaInitializer(impl: RxJavaInitializer): Initializer

    @Binds
    @IntoSet
    abstract fun bindWorkManagerInitializer(impl: WorkManagerInitializer): Initializer

    @Module
    companion object {
        @Provides
        @ElementsIntoSet
        @JvmStatic
        fun provideFabricInitializer(fabricInitializer: Provider<FabricInitializer>): Set<Initializer> =
            @Suppress("ConstantConditionIf")
            if (BuildConfig.GOOGLE_SERVICES_ENABLED) {
                setOf(fabricInitializer.get())
            } else {
                emptySet()
            }
    }
}
