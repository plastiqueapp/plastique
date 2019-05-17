package io.plastique

import io.plastique.core.BaseApplication
import io.plastique.inject.BaseAppComponent
import io.plastique.inject.components.AppComponent
import io.plastique.inject.components.DaggerReleaseAppComponent

class PlastiqueApplication : BaseApplication(), BaseAppComponent.Holder {
    override val appComponent: AppComponent by lazy(LazyThreadSafetyMode.NONE) {
        DaggerReleaseAppComponent.factory().create(this)
    }
}
