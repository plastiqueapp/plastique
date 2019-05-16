package io.plastique.core.init

import android.app.Application
import android.os.Build
import com.squareup.leakcanary.LeakCanary
import javax.inject.Inject

class LeakCanaryInitializer @Inject constructor(private val application: Application) : Initializer() {
    override fun initialize() {
        if (!isLeakCanaryDisabled) {
            LeakCanary.install(application)
        }
    }

    private val isLeakCanaryDisabled: Boolean
        // Samsung's Android 9.0.0 has too many memory leaks.
        get() = Build.MANUFACTURER.equals("Samsung", ignoreCase = true) && Build.VERSION.SDK_INT == Build.VERSION_CODES.P
}
