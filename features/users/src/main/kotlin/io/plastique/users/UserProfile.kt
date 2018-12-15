package io.plastique.users

data class UserProfile(
    val user: User,
    val profileUrl: String,
    val realName: String?,
    val isWatching: Boolean
)

fun UserProfileEntityWithRelations.toUserProfile(): UserProfile = UserProfile(
        user = users.first().toUser(),
        profileUrl = userProfile.profileUrl,
        realName = userProfile.realName,
        isWatching = userProfile.isWatching)
