package io.plastique.users

import io.reactivex.Single

interface UserRepository {
    fun getCurrentUser(accessToken: String): Single<User>

    fun getUserByName(username: String): Single<User>
}
