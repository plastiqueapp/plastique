package io.plastique.users

data class UserProfile(
    val user: User,
    val profileUrl: String,
    val realName: String?
)
