package io.plastique.test.inject

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import io.plastique.core.themes.ThemeManager
import io.plastique.inject.components.AppComponent
import io.plastique.inject.modules.ApiModule
import io.plastique.inject.modules.AppModule
import io.plastique.inject.modules.DatabaseModule
import io.plastique.inject.modules.DebugInitializerModule
import io.plastique.inject.modules.DebugOkHttpInterceptorModule
import io.plastique.inject.modules.DebuggingModule
import io.plastique.inject.modules.DeviationsModule
import io.plastique.inject.modules.NavigationModule
import io.plastique.inject.modules.NetworkModule
import io.plastique.inject.modules.ViewModelModule
import io.plastique.test.inject.modules.TestInitializerModule
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Singleton
@Component(modules = [
    ApiModule::class,
    AppModule::class,
    DatabaseModule::class,
    DebuggingModule::class,
    DebugInitializerModule::class,
    DebugOkHttpInterceptorModule::class,
    DeviationsModule::class,
    NavigationModule::class,
    NetworkModule::class,
    TestInitializerModule::class,
    ViewModelModule::class
])
interface TestAppComponent : AppComponent {
    fun okHttpClient(): OkHttpClient

    fun themeManager(): ThemeManager

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): TestAppComponent
    }
}
