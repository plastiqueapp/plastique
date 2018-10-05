package io.plastique.users

import io.plastique.api.users.User
import javax.inject.Inject

class UserMapper @Inject constructor() {
    fun map(user: User): UserEntity {
        return UserEntity(
                id = user.id,
                name = user.name,
                type = user.type,
                avatarUrl = user.avatarUrl)
    }
}
