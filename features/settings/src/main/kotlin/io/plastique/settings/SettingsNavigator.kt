package io.plastique.settings

import io.plastique.core.navigation.NavigationContext

interface SettingsNavigator {
    fun openLogin(navigationContext: NavigationContext)

    fun openPlayStore(navigationContext: NavigationContext, packageName: String)
}
