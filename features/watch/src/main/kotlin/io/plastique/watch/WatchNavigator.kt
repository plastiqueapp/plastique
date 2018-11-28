package io.plastique.watch

import io.plastique.core.navigation.NavigationContext
import io.plastique.users.User

interface WatchNavigator {
    fun openLogin(navigationContext: NavigationContext)

    fun openUserProfile(navigationContext: NavigationContext, user: User)
}
