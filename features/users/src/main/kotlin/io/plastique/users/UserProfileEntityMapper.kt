package io.plastique.users

import javax.inject.Inject

class UserProfileEntityMapper @Inject constructor(
    private val userEntityMapper: UserEntityMapper
) {
    fun map(userProfileWithUser: UserProfileWithUser): UserProfile {
        val userProfile = userProfileWithUser.userProfile
        val user = userProfileWithUser.user.first()
        if (userProfile.userId != user.id) {
            throw IllegalArgumentException("Expected user with id ${userProfile.userId} but got ${user.id}")
        }
        return UserProfile(
                user = userEntityMapper.map(user),
                profileUrl = userProfile.profileUrl,
                realName = userProfile.realName)
    }
}
