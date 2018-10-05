package io.plastique.core

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import io.plastique.core.ui.R

class BrowserLauncher(private val context: Context) {
    private val toolbarColor: Int

    init {
        val a = context.obtainStyledAttributes(intArrayOf(R.attr.colorPrimary))
        toolbarColor = a.getColor(0, 0)
        a.recycle()
    }

    fun openUrl(url: String) {
        CustomTabsIntent.Builder()
                .setToolbarColor(toolbarColor)
                .setShowTitle(true)
                .setInstantAppsEnabled(false)
                .build()
                .launchUrl(context, Uri.parse(url))
    }
}
