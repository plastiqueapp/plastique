package io.plastique.main

import io.plastique.core.navigation.NavigationContext

interface MainNavigator {
    fun openSettings(navigationContext: NavigationContext)
}
