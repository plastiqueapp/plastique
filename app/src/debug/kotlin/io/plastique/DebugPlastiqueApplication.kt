package io.plastique

import android.os.Build
import com.facebook.stetho.Stetho
import com.sch.stetho.timber.StethoTree
import com.squareup.leakcanary.LeakCanary
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

        if (!isLeakCanaryDisabled) {
            LeakCanary.install(this)
        }
    }

    private val isLeakCanaryDisabled: Boolean
        // Samsung's Android 9.0.0 has too many memory leaks.
        get() = Build.MANUFACTURER.equals("Samsung", ignoreCase = true) && Build.VERSION.SDK_INT == 28
}
