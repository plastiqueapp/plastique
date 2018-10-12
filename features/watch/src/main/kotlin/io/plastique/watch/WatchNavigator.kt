package io.plastique.watch

import io.plastique.core.navigation.NavigationContext

interface WatchNavigator {
    fun openLogin(navigationContext: NavigationContext)

    fun openUserProfile(navigationContext: NavigationContext, username: String)
}
