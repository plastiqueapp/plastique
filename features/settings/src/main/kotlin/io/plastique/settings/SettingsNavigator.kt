package io.plastique.settings

import io.plastique.core.navigation.NavigationContext

interface SettingsNavigator {
    fun openPlayStore(navigationContext: NavigationContext, packageName: String)
}
