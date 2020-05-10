package io.plastique.auth

import io.plastique.core.navigation.Navigator

interface AuthNavigator : Navigator {
    fun openUrl(url: String)
}
