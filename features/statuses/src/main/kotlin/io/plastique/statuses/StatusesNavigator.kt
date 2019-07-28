package io.plastique.statuses

import io.plastique.comments.CommentThreadId
import io.plastique.core.navigation.Navigator

interface StatusesNavigator : Navigator {
    fun openComments(threadId: CommentThreadId)

    fun openDeviation(deviationId: String)

    fun openStatus(statusId: String)

    fun openPostStatus(shareObjectId: ShareObjectId?)
}
