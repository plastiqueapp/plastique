package io.plastique.core.init

import android.content.Context
import com.jakewharton.threetenabp.AndroidThreeTen
import javax.inject.Inject

class AndroidThreeTenInitializer @Inject constructor(private val context: Context) : Initializer() {
    override fun initialize() {
        AndroidThreeTen.init(context)
    }
}
