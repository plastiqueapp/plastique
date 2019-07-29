package io.plastique.deviations

import io.plastique.comments.CommentThreadId
import io.plastique.core.navigation.Navigator
import io.plastique.deviations.categories.Category
import io.plastique.statuses.ShareObjectId
import io.plastique.users.User

interface DeviationsNavigator : Navigator {
    fun openCategoryList(selectedCategory: Category, requestCode: Int)

    fun openComments(threadId: CommentThreadId)

    fun openDeviation(deviationId: String)

    fun openDeviationInfo(deviationId: String)

    fun openPostStatus(shareObjectId: ShareObjectId?)

    fun openSignIn()

    fun openTag(tag: String)

    fun openUserProfile(user: User)

    fun openUrl(url: String)

    fun showTimeRangeDialog(tag: String)
}
