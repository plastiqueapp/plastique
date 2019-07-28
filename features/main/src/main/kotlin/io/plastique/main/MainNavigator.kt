package io.plastique.main

import io.plastique.core.navigation.Navigator
import io.plastique.users.User

interface MainNavigator : Navigator {
    fun openSettings()

    fun openUserProfile(user: User)
}
