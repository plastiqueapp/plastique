package io.plastique

import android.os.Build
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.soloader.SoLoader
import com.facebook.stetho.Stetho
import com.sch.stetho.timber.StethoTree
import com.squareup.leakcanary.LeakCanary
import io.plastique.inject.AppComponent
import io.plastique.inject.components.DaggerDebugModuleAppComponent
import io.plastique.inject.components.DebugModuleAppComponent
import timber.log.Timber

class DebugPlastiqueApplication : BasePlastiqueApplication(), AppComponent.Holder {
    override val appComponent: DebugModuleAppComponent by lazy(LazyThreadSafetyMode.NONE) {
        DaggerDebugModuleAppComponent.builder()
                .application(this)
                .build()
    }

    override fun init() {
        super.init()

        SoLoader.init(this, false)
        if (FlipperUtils.shouldEnableFlipper(this)) {
            appComponent.flipperClient().start()
        }

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
