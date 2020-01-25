package io.plastique.inject.modules

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import io.plastique.core.analytics.AnalyticsInitializer
import io.plastique.core.init.AndroidThreeTenInitializer
import io.plastique.core.init.Initializer
import io.plastique.core.init.RxJavaInitializer
import io.plastique.core.init.TimberInitializer
import io.plastique.core.themes.ThemeInitializer
import io.plastique.core.work.WorkManagerInitializer

@Module
interface InitializerModule {
    @Binds
    @IntoSet
    fun bindAnalyticsInitializer(impl: AnalyticsInitializer): Initializer

    @Binds
    @IntoSet
    fun bindAndroidThreeTenInitializer(impl: AndroidThreeTenInitializer): Initializer

    @Binds
    @IntoSet
    fun bindRxJavaInitializer(impl: RxJavaInitializer): Initializer

    @Binds
    @IntoSet
    fun bindThemeInitializer(impl: ThemeInitializer): Initializer

    @Binds
    @IntoSet
    fun bindTimberInitializer(impl: TimberInitializer): Initializer

    @Binds
    @IntoSet
    fun bindWorkManagerInitializer(impl: WorkManagerInitializer): Initializer
}
