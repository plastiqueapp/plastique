package io.plastique.users

import io.plastique.users.profile.UserProfileActivity

interface UsersActivityComponent {
    fun inject(activity: UserProfileActivity)
}
