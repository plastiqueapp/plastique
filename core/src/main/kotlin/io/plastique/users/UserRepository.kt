package io.plastique.users

import io.plastique.api.users.UserDto
import io.reactivex.Single

interface UserRepository {
    fun getCurrentUser(accessToken: String): Single<User>

    fun getUserByName(username: String): Single<User>

    fun put(user: UserDto)

    fun put(users: Collection<UserDto>)

    fun setWatching(username: String, watching: Boolean)
}
