package io.plastique.comments

import io.plastique.core.navigation.Navigator
import io.plastique.users.User

interface CommentsNavigator : Navigator {
    fun openSignIn()

    fun openUserProfile(user: User)
}
