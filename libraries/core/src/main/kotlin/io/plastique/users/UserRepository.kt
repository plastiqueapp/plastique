package io.plastique.users

import io.plastique.api.users.UserDto
import io.reactivex.Observable

interface UserRepository {
    fun getCurrentUser(userId: String): Observable<User>

    fun persistWithTimestamp(user: UserDto)

    fun put(users: Collection<UserDto>)

    fun setWatching(username: String, watching: Boolean)
}
