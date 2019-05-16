package io.plastique

import io.plastique.inject.AppComponent
import io.plastique.inject.components.DaggerDebugModuleAppComponent
import io.plastique.inject.components.DebugModuleAppComponent

class DebugPlastiqueApplication : BasePlastiqueApplication(), AppComponent.Holder {
    override val appComponent: DebugModuleAppComponent by lazy(LazyThreadSafetyMode.NONE) {
        DaggerDebugModuleAppComponent.factory().create(this)
    }
}
