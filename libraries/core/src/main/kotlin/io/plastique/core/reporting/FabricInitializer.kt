package io.plastique.core.reporting

import android.content.Context
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import io.fabric.sdk.android.Fabric
import io.plastique.core.BuildConfig
import io.plastique.core.init.Initializer
import timber.log.Timber
import javax.inject.Inject

class FabricInitializer @Inject constructor(private val context: Context) : Initializer() {
    override fun initialize() {
        val crashlytics = Crashlytics.Builder()
            .core(CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build())
            .build()
        Fabric.with(Fabric.Builder(context)
            .kits(crashlytics)
            .debuggable(BuildConfig.DEBUG)
            .build())

        Timber.plant(CrashlyticsTree())
    }
}
