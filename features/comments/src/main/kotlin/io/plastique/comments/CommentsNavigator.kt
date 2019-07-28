package io.plastique.comments

import io.plastique.core.navigation.Navigator
import io.plastique.users.User

interface CommentsNavigator : Navigator {
    fun openLogin()

    fun openUserProfile(user: User)
}
