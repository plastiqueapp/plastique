package io.plastique.deviations

import io.plastique.comments.CommentThreadId
import io.plastique.core.navigation.NavigationContext
import io.plastique.statuses.ShareObjectId
import io.plastique.users.User

interface DeviationsNavigator {
    fun openComments(navigationContext: NavigationContext, threadId: CommentThreadId)

    fun openDeviation(navigationContext: NavigationContext, deviationId: String)

    fun openDeviationInfo(navigationContext: NavigationContext, deviationId: String)

    fun openLogin(navigationContext: NavigationContext)

    fun openPostStatus(navigationContext: NavigationContext, shareObjectId: ShareObjectId?)

    fun openTag(navigationContext: NavigationContext, tag: String)

    fun openUserProfile(navigationContext: NavigationContext, user: User)

    fun openUrl(navigationContext: NavigationContext, url: String)
}
