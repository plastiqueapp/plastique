package io.plastique

import io.plastique.inject.AppComponent
import io.plastique.inject.components.DaggerModuleAppComponent
import io.plastique.inject.components.ModuleAppComponent

class PlastiqueApplication : BasePlastiqueApplication(), AppComponent.Holder {
    override val appComponent: ModuleAppComponent by lazy(LazyThreadSafetyMode.NONE) {
        DaggerModuleAppComponent.builder()
                .application(this)
                .build()
    }
}
