package io.plastique.core

import android.app.Application
import io.plastique.inject.BaseActivityComponent
import io.plastique.inject.BaseAppComponent

abstract class BaseApplication : Application(), BaseAppComponent.Holder, BaseActivityComponent.Factory {
    override fun onCreate() {
        super.onCreate()

        appComponent.initializers().asSequence()
            .sortedByDescending { it.priority }
            .forEach { it.initialize() }
    }

    override fun createActivityComponent(): BaseActivityComponent {
        return appComponent.createActivityComponent()
    }
}
