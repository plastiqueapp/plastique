package io.plastique.users.profile

import io.plastique.users.User
import io.plastique.users.toUser

data class UserProfile(
    val user: User,
    val url: String,
    val realName: String?,
    val isWatching: Boolean
)

fun UserProfileEntityWithRelations.toUserProfile(): UserProfile = UserProfile(
        user = users.first().toUser(),
        url = userProfile.url,
        realName = userProfile.realName,
        isWatching = userProfile.isWatching)
