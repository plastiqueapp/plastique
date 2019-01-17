package io.plastique.users

import io.plastique.core.navigation.NavigationContext

interface UsersNavigator {
    fun openWatchers(navigationContext: NavigationContext, username: String?)

    fun openUrl(navigationContext: NavigationContext, url: String)
}
