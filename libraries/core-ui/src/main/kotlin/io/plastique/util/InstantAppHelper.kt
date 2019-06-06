package io.plastique.util

import android.app.Activity
import android.content.Context
import com.google.android.gms.instantapps.InstantApps
import dagger.Reusable
import javax.inject.Inject

@Reusable
class InstantAppHelper @Inject constructor(private val context: Context) {
    val isInstantApp: Boolean
        get() = InstantApps.getPackageManagerCompat(context).isInstantApp

    fun showInstallPrompt(activity: Activity) {
        InstantApps.showInstallPrompt(activity, null, 0, INSTALL_REFERER)
    }

    companion object {
        private const val INSTALL_REFERER = "instant-app"
    }
}
