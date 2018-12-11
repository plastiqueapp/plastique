package io.plastique.feed

import io.plastique.comments.CommentThreadId
import io.plastique.core.navigation.NavigationContext
import io.plastique.statuses.ShareObjectId
import io.plastique.users.User

interface FeedNavigator {
    fun openCollectionFolder(navigationContext: NavigationContext, username: String?, folderId: String, folderName: String)

    fun openComments(navigationContext: NavigationContext, threadId: CommentThreadId)

    fun openDeviation(navigationContext: NavigationContext, deviationId: String)

    fun openLogin(navigationContext: NavigationContext)

    fun openStatus(navigationContext: NavigationContext, statusId: String)

    fun openPostStatus(navigationContext: NavigationContext, shareObjectId: ShareObjectId?)

    fun openUserProfile(navigationContext: NavigationContext, user: User)
}
