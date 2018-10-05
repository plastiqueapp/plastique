package io.plastique.inject.components

import dagger.Component
import io.plastique.inject.modules.ApiModule
import io.plastique.inject.modules.AppModule
import io.plastique.inject.modules.DaoModule
import io.plastique.inject.modules.DatabaseModule
import io.plastique.inject.modules.DebugOkHttpModule
import io.plastique.inject.modules.DeviationsModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    ApiModule::class,
    AppModule::class,
    DatabaseModule::class,
    DaoModule::class,
    DebugOkHttpModule::class,
    DeviationsModule::class
])
interface DebugModuleAppComponent : ModuleAppComponent {
    @Component.Builder
    interface Builder : ModuleAppComponent.Builder
}
