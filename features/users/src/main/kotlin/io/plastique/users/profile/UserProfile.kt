package io.plastique.users.profile

import io.plastique.users.User

data class UserProfile(
    val user: User,
    val url: String,
    val realName: String?,
    val bio: String?,
    val isWatching: Boolean,
    val stats: Stats
) {
    data class Stats(
        val deviations: Int,
        val favorites: Int,
        val watchers: Int
    )
}
