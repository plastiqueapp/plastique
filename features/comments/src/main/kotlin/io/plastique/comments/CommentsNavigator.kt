package io.plastique.comments

import io.plastique.core.navigation.NavigationContext
import io.plastique.users.User

interface CommentsNavigator {
    fun openLogin(navigationContext: NavigationContext)

    fun openUserProfile(navigationContext: NavigationContext, user: User)
}
