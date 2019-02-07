package io.plastique.main

import io.plastique.core.navigation.NavigationContext
import io.plastique.users.User

interface MainNavigator {
    fun openSettings(navigationContext: NavigationContext)

    fun openUserProfile(navigationContext: NavigationContext, user: User)
}
