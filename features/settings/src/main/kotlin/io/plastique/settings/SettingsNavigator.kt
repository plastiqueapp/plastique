package io.plastique.settings

import io.plastique.core.navigation.Navigator

interface SettingsNavigator : Navigator {
    fun openLicenses()

    fun openPlayStore(packageName: String)

    fun openSignIn()

    fun openUrl(url: String)
}
