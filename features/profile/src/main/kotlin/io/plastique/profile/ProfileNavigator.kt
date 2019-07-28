package io.plastique.profile

import io.plastique.core.navigation.Navigator

interface ProfileNavigator : Navigator {
    fun openSignIn()

    fun openWatchers(username: String?)
}
