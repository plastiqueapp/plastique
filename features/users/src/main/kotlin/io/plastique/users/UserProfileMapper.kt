package io.plastique.users

import io.plastique.api.users.UserProfile
import javax.inject.Inject

class UserProfileMapper @Inject constructor() {
    fun map(userProfile: UserProfile): UserProfileEntity {
        return UserProfileEntity(
                userId = userProfile.user.id,
                profileUrl = userProfile.profileUrl,
                realName = userProfile.realName)
    }
}
