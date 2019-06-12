package io.plastique.core.reporting

import android.content.Context
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import io.fabric.sdk.android.Fabric
import io.plastique.core.init.Initializer
import timber.log.Timber

class FabricInitializer(private val context: Context, private val debug: Boolean) : Initializer() {
    override fun initialize() {
        val crashlytics = Crashlytics.Builder()
            .core(CrashlyticsCore.Builder()
                .disabled(debug)
                .build())
            .build()
        Fabric.with(Fabric.Builder(context)
            .kits(crashlytics)
            .debuggable(debug)
            .build())

        Timber.plant(CrashlyticsTree())
    }
}
