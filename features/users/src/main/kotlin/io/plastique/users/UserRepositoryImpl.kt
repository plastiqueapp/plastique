package io.plastique.users

import io.plastique.api.users.UserService
import io.reactivex.Single
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userService: UserService
) : UserRepository {

    override fun getCurrentUser(accessToken: String): Single<User> {
        return userService.whoami(accessToken)
                .map { user -> user.toUserEntity().also { put(it) }.toUser() }
    }

    override fun getUserByName(username: String): Single<User> {
        return userDao.getUserByName(username)
                .switchIfEmpty(getUserByNameFromServer(username))
                .map { userEntity -> userEntity.toUser() }
    }

    private fun getUserByNameFromServer(username: String): Single<UserEntity> {
        return userService.getUserProfile(username)
                .map { userProfile -> userProfile.user.toUserEntity().also { put(it) } }
    }

    override fun put(user: UserEntity) {
        userDao.insertOrUpdate(user)
    }

    override fun put(users: Collection<UserEntity>) {
        if (users.isEmpty()) {
            return
        }
        userDao.insertOrUpdate(users)
    }
}
