package io.plastique.test.inject.modules

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import io.plastique.core.init.Initializer
import io.plastique.test.RxIdlerInitializer

@Module
interface TestInitializerModule {
    @Binds
    @IntoSet
    fun bindRxIdlerInitializer(impl: RxIdlerInitializer): Initializer
}
