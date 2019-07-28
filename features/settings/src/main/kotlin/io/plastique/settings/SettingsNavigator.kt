package io.plastique.settings

import io.plastique.core.navigation.Navigator

interface SettingsNavigator : Navigator {
    fun openLicenses()

    fun openLogin()

    fun openPlayStore(packageName: String)

    fun openUrl(url: String)
}
