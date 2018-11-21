package io.plastique.deviations

import io.plastique.comments.CommentThreadId
import io.plastique.core.navigation.NavigationContext

interface DeviationsNavigator {
    fun openComments(navigationContext: NavigationContext, threadId: CommentThreadId)

    fun openDeviation(navigationContext: NavigationContext, deviationId: String)

    fun openUserProfile(navigationContext: NavigationContext, username: String)
}
