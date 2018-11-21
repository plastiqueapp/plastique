package io.plastique.users

import io.plastique.comments.CommentThreadId
import io.plastique.core.navigation.NavigationContext

interface UsersNavigator {
    fun openCollections(navigationContext: NavigationContext, username: String)

    fun openComments(navigationContext: NavigationContext, threadId: CommentThreadId)

    fun openGallery(navigationContext: NavigationContext, username: String)

    fun openWatchers(navigationContext: NavigationContext, username: String?)
}
