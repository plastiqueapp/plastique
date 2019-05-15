package io.plastique.core.browser

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import io.plastique.core.ui.R
import javax.inject.Inject

class BrowserLauncher @Inject constructor() {
    fun openUrl(context: Context, url: String) {
        val a = context.obtainStyledAttributes(intArrayOf(R.attr.colorPrimary))
        val toolbarColor = a.getColor(0, 0)
        a.recycle()

        CustomTabsIntent.Builder()
            .setToolbarColor(toolbarColor)
            .setShowTitle(true)
            .setInstantAppsEnabled(false)
            .build()
            .launchUrl(context, Uri.parse(url))
    }
}
