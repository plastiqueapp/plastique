package io.plastique.deviations

import io.plastique.comments.CommentThreadId
import io.plastique.core.navigation.NavigationContext
import io.plastique.users.User

interface DeviationsNavigator {
    fun openComments(navigationContext: NavigationContext, threadId: CommentThreadId)

    fun openDeviation(navigationContext: NavigationContext, deviationId: String)

    fun openUserProfile(navigationContext: NavigationContext, user: User)
}
