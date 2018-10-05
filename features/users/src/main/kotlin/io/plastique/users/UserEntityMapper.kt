package io.plastique.users

import javax.inject.Inject

class UserEntityMapper @Inject constructor() {
    fun map(user: UserEntity): User {
        return User(
                id = user.id,
                name = user.name,
                type = user.type,
                avatarUrl = user.avatarUrl)
    }
}
