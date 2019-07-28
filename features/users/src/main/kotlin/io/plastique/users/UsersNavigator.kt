package io.plastique.users

import io.plastique.core.navigation.Navigator

interface UsersNavigator : Navigator {
    fun openSignIn()

    fun openUrl(url: String)

    fun openWatchers(username: String?)
}
