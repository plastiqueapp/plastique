package io.plastique.users

import io.plastique.api.users.UserService
import io.reactivex.Single
import javax.inject.Inject
import io.plastique.api.users.User as UserDto

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userService: UserService
) : UserRepository {

    override fun getCurrentUser(accessToken: String): Single<User> {
        return userService.whoami(accessToken)
                .map { user -> persistUser(user.toUserEntity()) }
                .map { userEntity -> userEntity.toUser() }
    }

    override fun getUserByName(username: String): Single<User> {
        return userDao.getUserByName(username)
                .switchIfEmpty(getUserByNameFromServer(username))
                .map { userEntity -> userEntity.toUser() }
    }

    private fun getUserByNameFromServer(username: String): Single<UserEntity> {
        return userService.getUserProfile(username)
                .map { userProfile -> persistUser(userProfile.user.toUserEntity()) }
    }

    private fun persistUser(userEntity: UserEntity): UserEntity {
        userDao.insertOrUpdate(userEntity)
        return userEntity
    }
}
