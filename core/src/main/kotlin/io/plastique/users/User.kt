package io.plastique.users

data class User(
    val id: String,
    val name: String,
    val type: String,
    val avatarUrl: String
)

fun UserEntity.toUser(): User = User(id = id, name = name, type = type, avatarUrl = avatarUrl)
