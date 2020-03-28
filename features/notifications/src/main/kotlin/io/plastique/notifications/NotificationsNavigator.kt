package io.plastique.notifications

import io.plastique.collections.folders.CollectionFolderId
import io.plastique.core.navigation.Navigator
import io.plastique.users.User

interface NotificationsNavigator : Navigator {
    fun openCollectionFolder(folderId: CollectionFolderId, folderName: String)

    fun openDeviation(deviationId: String)

    fun openSignIn()

    fun openStatus(statusId: String)

    fun openUserProfile(user: User)
}
