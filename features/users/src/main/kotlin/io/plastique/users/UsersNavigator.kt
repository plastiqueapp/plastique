package io.plastique.users

import io.plastique.core.navigation.NavigationContext

interface UsersNavigator {
    fun openUrl(navigationContext: NavigationContext, url: String)

    fun openWatchers(navigationContext: NavigationContext, username: String?)
}
