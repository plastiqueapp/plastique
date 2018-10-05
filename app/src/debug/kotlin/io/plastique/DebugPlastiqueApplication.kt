package io.plastique

import com.facebook.stetho.Stetho
import com.sch.stetho.timber.StethoTree
import io.plastique.inject.AppComponent
import io.plastique.inject.components.DaggerDebugModuleAppComponent
import io.plastique.inject.components.ModuleAppComponent
import timber.log.Timber

class DebugPlastiqueApplication : BasePlastiqueApplication(), AppComponent.Holder {
    override val appComponent: ModuleAppComponent by lazy(LazyThreadSafetyMode.NONE) {
        DaggerDebugModuleAppComponent.builder()
                .application(this)
                .build()
    }

    override fun init() {
        super.init()

        Stetho.initializeWithDefaults(this)
        Timber.plant(Timber.DebugTree())
        Timber.plant(StethoTree())
    }
}
