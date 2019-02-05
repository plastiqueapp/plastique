package io.plastique.users

import io.plastique.api.users.UserDto
import io.plastique.api.users.UserService
import io.reactivex.Single
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userService: UserService
) : UserRepository {

    override fun getCurrentUser(accessToken: String): Single<User> {
        return userService.whoami(accessToken)
                .map { user -> user.toUserEntity().also { userDao.insertOrUpdate(it) }.toUser() }
    }

    override fun put(user: UserDto) {
        userDao.insertOrUpdate(user.toUserEntity())
    }

    override fun put(users: Collection<UserDto>) {
        if (users.isEmpty()) {
            return
        }
        userDao.insertOrUpdate(users.map { it.toUserEntity() })
    }

    override fun setWatching(username: String, watching: Boolean) {
        userDao.setWatching(username, watching)
    }
}
