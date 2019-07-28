package io.plastique.inject.components

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import io.plastique.inject.modules.ApiModule
import io.plastique.inject.modules.AppModule
import io.plastique.inject.modules.DatabaseModule
import io.plastique.inject.modules.DeviationsModule
import io.plastique.inject.modules.InitializerModule
import io.plastique.inject.modules.NavigationModule
import io.plastique.inject.modules.NetworkModule
import io.plastique.inject.modules.OkHttpInterceptorModule
import io.plastique.inject.modules.ViewModelModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    ApiModule::class,
    AppModule::class,
    DatabaseModule::class,
    DeviationsModule::class,
    InitializerModule::class,
    NavigationModule::class,
    NetworkModule::class,
    OkHttpInterceptorModule::class,
    ViewModelModule::class
])
interface ReleaseAppComponent : AppComponent {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): ReleaseAppComponent
    }
}
