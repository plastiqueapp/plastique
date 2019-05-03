package io.plastique.inject.components

import android.app.Application
import com.facebook.flipper.core.FlipperClient
import dagger.BindsInstance
import dagger.Component
import io.plastique.inject.modules.ApiModule
import io.plastique.inject.modules.AppModule
import io.plastique.inject.modules.DaoModule
import io.plastique.inject.modules.DatabaseModule
import io.plastique.inject.modules.DebugOkHttpInterceptorModule
import io.plastique.inject.modules.DebuggingModule
import io.plastique.inject.modules.DeviationsModule
import io.plastique.inject.modules.NetworkModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    ApiModule::class,
    AppModule::class,
    DatabaseModule::class,
    DaoModule::class,
    DebuggingModule::class,
    DebugOkHttpInterceptorModule::class,
    DeviationsModule::class,
    NetworkModule::class
])
interface DebugModuleAppComponent : ModuleAppComponent {
    fun flipperClient(): FlipperClient

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): DebugModuleAppComponent
    }
}
