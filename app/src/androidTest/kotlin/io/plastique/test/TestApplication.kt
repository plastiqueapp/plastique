package io.plastique.test

import io.plastique.core.BaseApplication
import io.plastique.inject.BaseAppComponent
import io.plastique.inject.components.AppComponent
import io.plastique.test.inject.DaggerTestAppComponent

class TestApplication : BaseApplication(), BaseAppComponent.Holder {
    override val appComponent: AppComponent by lazy(LazyThreadSafetyMode.NONE) {
        DaggerTestAppComponent.factory().create(this)
    }
}
