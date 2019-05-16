package io.plastique

import android.app.Application
import com.squareup.leakcanary.LeakCanary
import io.plastique.core.init.Initializer
import io.plastique.inject.ActivityComponent
import io.plastique.inject.AppComponent
import io.plastique.inject.components.ModuleAppComponent
import io.plastique.inject.getComponent
import javax.inject.Inject

abstract class BasePlastiqueApplication : Application(), AppComponent.Holder, ActivityComponent.Factory {
    @Inject lateinit var initializers: Set<@JvmSuppressWildcards Initializer>

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }

        injectDependencies()

        initializers.asSequence()
            .sortedByDescending { it.priority }
            .forEach { it.initialize() }
    }

    private fun injectDependencies() {
        getComponent<ModuleAppComponent>().inject(this)
    }

    override fun createActivityComponent(): ActivityComponent {
        return appComponent.createActivityComponent()
    }
}
