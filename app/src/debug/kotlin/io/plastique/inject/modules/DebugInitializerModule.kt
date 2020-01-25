package io.plastique.inject.modules

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import io.plastique.core.init.FlipperInitializer
import io.plastique.core.init.Initializer
import timber.log.Timber

@Module(includes = [InitializerModule::class])
abstract class DebugInitializerModule {
    @Binds
    @IntoSet
    abstract fun bindFlipperInitializer(impl: FlipperInitializer): Initializer

    @Module
    companion object {
        @Provides
        @IntoSet
        @JvmStatic
        fun provideDebugTree(): Timber.Tree = Timber.DebugTree()
    }
}
