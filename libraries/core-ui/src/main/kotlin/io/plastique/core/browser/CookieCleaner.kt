package io.plastique.core.browser

import android.webkit.CookieManager
import javax.inject.Inject

class CookieCleaner @Inject constructor(
    private val cookieManager: CookieManager
) {
    fun clean() {
        cookieManager.removeAllCookies(null)
    }
}
