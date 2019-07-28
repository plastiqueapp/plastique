package io.plastique.watch

import io.plastique.core.navigation.Navigator
import io.plastique.users.User

interface WatchNavigator : Navigator {
    fun openSignIn()

    fun openUserProfile(user: User)
}
