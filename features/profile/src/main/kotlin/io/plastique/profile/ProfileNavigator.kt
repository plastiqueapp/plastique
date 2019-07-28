package io.plastique.profile

import io.plastique.core.navigation.Navigator

interface ProfileNavigator : Navigator {
    fun openLogin()

    fun openWatchers(username: String?)
}
