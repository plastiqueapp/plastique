package io.plastique.core

import android.app.Application
import com.squareup.leakcanary.LeakCanary
import io.plastique.inject.BaseActivityComponent
import io.plastique.inject.BaseAppComponent

abstract class BaseApplication : Application(), BaseAppComponent.Holder, BaseActivityComponent.Factory {
    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }

        appComponent.initializers().asSequence()
            .sortedByDescending { it.priority }
            .forEach { it.initialize() }
    }

    override fun createActivityComponent(): BaseActivityComponent {
        return appComponent.createActivityComponent()
    }
}
