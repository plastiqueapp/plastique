package io.plastique.notifications

import io.plastique.core.navigation.Navigator
import io.plastique.users.User

interface NotificationsNavigator : Navigator {
    fun openCollectionFolder(username: String?, folderId: String, folderName: String)

    fun openDeviation(deviationId: String)

    fun openSignIn()

    fun openStatus(statusId: String)

    fun openUserProfile(user: User)
}
