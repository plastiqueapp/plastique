package io.plastique.feed

import io.plastique.comments.CommentThreadId
import io.plastique.core.navigation.Navigator
import io.plastique.statuses.ShareObjectId
import io.plastique.users.User

interface FeedNavigator : Navigator {
    fun openCollectionFolder(username: String?, folderId: String, folderName: String)

    fun openComments(threadId: CommentThreadId)

    fun openDeviation(deviationId: String)

    fun openSignIn()

    fun openStatus(statusId: String)

    fun openPostStatus(shareObjectId: ShareObjectId?)

    fun openUserProfile(user: User)
}
