package io.plastique.comments

import io.plastique.core.navigation.NavigationContext

interface CommentsNavigator {
    fun openLogin(navigationContext: NavigationContext)

    fun openUserProfile(navigationContext: NavigationContext, username: String)
}
