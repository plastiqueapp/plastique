package io.plastique.core.init

import android.content.Context
import com.facebook.stetho.Stetho
import com.sch.stetho.timber.StethoTree
import timber.log.Timber
import javax.inject.Inject

class StethoInitializer @Inject constructor(private val context: Context) : Initializer() {
    override fun initialize() {
        Stetho.initializeWithDefaults(context)
        Timber.plant(StethoTree())
    }
}
