package io.plastique.test

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

class PlastiqueJUnitRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader, className: String, context: Context): Application {
        return super.newApplication(cl, TestApplication::class.java.canonicalName, context)
    }
}
