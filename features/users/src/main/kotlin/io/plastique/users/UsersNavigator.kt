package io.plastique.users

import io.plastique.core.navigation.NavigationContext

interface UsersNavigator {
    fun openCollections(navigationContext: NavigationContext, username: String)

    fun openGallery(navigationContext: NavigationContext, username: String)

    fun openCommentsForUserProfile(navigationContext: NavigationContext, username: String)
}
