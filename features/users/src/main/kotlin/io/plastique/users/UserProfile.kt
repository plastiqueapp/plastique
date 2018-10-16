package io.plastique.users

data class UserProfile(
    val user: User,
    val profileUrl: String,
    val realName: String?
)

fun UserProfileWithUser.toUserProfile(): UserProfile = UserProfile(
        user = user.first().toUser(),
        profileUrl = userProfile.profileUrl,
        realName = userProfile.realName)
