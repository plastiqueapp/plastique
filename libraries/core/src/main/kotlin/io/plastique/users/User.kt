package io.plastique.users

data class User(
    val id: String,
    val name: String,
    val type: UserType,
    val avatarUrl: String?
)

enum class UserType {
    Regular,
    Group,
    Core,
    Banned
}

fun UserEntity.toUser(): User {
    val userType = when (type) {
        "premium",
        "senior",
        "beta",
        "hell",
        "hell-beta",
        "admin" -> UserType.Core
        "group" -> UserType.Group
        "banned" -> UserType.Banned
        else -> UserType.Regular
    }
    val avatarUrl = if (avatarUrl != "https://a.deviantart.net/avatars/default.gif") avatarUrl else null
    return User(id = id, name = name, type = userType, avatarUrl = avatarUrl)
}
