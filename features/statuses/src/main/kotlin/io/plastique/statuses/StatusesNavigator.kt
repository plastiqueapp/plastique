package io.plastique.statuses

import io.plastique.comments.CommentThreadId
import io.plastique.core.navigation.NavigationContext

interface StatusesNavigator {
    fun openComments(navigationContext: NavigationContext, threadId: CommentThreadId)

    fun openDeviation(navigationContext: NavigationContext, deviationId: String)

    fun openStatus(navigationContext: NavigationContext, statusId: String)

    fun openPostStatus(navigationContext: NavigationContext, shareObjectId: ShareObjectId?)
}
