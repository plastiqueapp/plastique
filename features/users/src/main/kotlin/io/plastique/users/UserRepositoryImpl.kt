package io.plastique.users

import androidx.room.RoomDatabase
import io.plastique.api.users.UserService
import io.reactivex.Single
import javax.inject.Inject
import io.plastique.api.users.User as UserDto

class UserRepositoryImpl @Inject constructor(
    private val database: RoomDatabase,
    private val userDao: UserDao,
    private val userService: UserService,
    private val userMapper: UserMapper,
    private val userEntityMapper: UserEntityMapper
) : UserRepository {

    override fun getCurrentUser(accessToken: String): Single<User> {
        return userService.whoami(accessToken)
                .map { user -> persistUser(userMapper.map(user)) }
                .map { userEntity -> userEntityMapper.map(userEntity) }
    }

    override fun getUserByName(username: String): Single<User> {
        return userDao.getUserByName(username)
                .switchIfEmpty(getUserByNameFromServer(username))
                .map { userEntity -> userEntityMapper.map(userEntity) }
    }

    private fun getUserByNameFromServer(username: String): Single<UserEntity> {
        return userService.getUserProfile(username)
                .map { userProfile -> persistUser(userMapper.map(userProfile.user)) }
    }

    private fun persistUser(userEntity: UserEntity): UserEntity {
        database.runInTransaction {
            userDao.insertOrUpdate(userEntity)
        }
        return userEntity
    }
}
