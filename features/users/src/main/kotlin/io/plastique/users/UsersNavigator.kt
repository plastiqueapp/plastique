package io.plastique.users

import io.plastique.core.navigation.Navigator

interface UsersNavigator : Navigator {
    fun openLogin()

    fun openUrl(url: String)

    fun openWatchers(username: String?)
}
