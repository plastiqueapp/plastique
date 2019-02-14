package io.plastique.notifications

import io.plastique.core.navigation.NavigationContext
import io.plastique.users.User

interface NotificationsNavigator {
    fun openCollectionFolder(navigationContext: NavigationContext, username: String?, folderId: String, folderName: String)

    fun openDeviation(navigationContext: NavigationContext, deviationId: String)

    fun openLogin(navigationContext: NavigationContext)

    fun openStatus(navigationContext: NavigationContext, statusId: String)

    fun openUserProfile(navigationContext: NavigationContext, user: User)
}
