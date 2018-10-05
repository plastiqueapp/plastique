package io.plastique.profile

import io.plastique.core.navigation.NavigationContext

interface ProfileNavigator {
    fun openLogin(navigationContext: NavigationContext)
}
