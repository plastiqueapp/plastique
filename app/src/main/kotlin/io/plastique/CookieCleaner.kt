package io.plastique

import android.webkit.CookieManager
import io.plastique.core.session.OnLogoutListener
import javax.inject.Inject

class CookieCleaner @Inject constructor(
    private val cookieManager: CookieManager
) : OnLogoutListener {

    override fun onLogout() {
        cookieManager.removeAllCookies(null)
    }
}
