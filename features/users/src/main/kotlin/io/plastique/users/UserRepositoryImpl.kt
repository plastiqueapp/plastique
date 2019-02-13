package io.plastique.users

import androidx.room.RoomDatabase
import io.plastique.api.users.UserDto
import io.plastique.api.users.UserService
import io.plastique.core.cache.CacheEntry
import io.plastique.core.cache.CacheEntryRepository
import io.plastique.core.cache.CacheHelper
import io.plastique.core.cache.DurationBasedCacheEntryChecker
import io.plastique.util.TimeProvider
import io.reactivex.Completable
import io.reactivex.Observable
import org.threeten.bp.Duration
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val database: RoomDatabase,
    private val userDao: UserDao,
    private val userService: UserService,
    private val cacheEntryRepository: CacheEntryRepository,
    private val timeProvider: TimeProvider
) : UserRepository {

    override fun getCurrentUser(userId: String): Observable<User> {
        val cacheHelper = CacheHelper(cacheEntryRepository, DurationBasedCacheEntryChecker(timeProvider, CACHE_DURATION))
        return cacheHelper.createObservable(
                cacheKey = getCacheKey(userId),
                cachedData = getUserByIdFromDb(userId),
                updater = fetchCurrentUser(userId))
    }

    private fun getUserByIdFromDb(userId: String): Observable<User> {
        return userDao.getUserById(userId)
                .filter { it.isNotEmpty() }
                .map { it.first().toUser() }
                .distinctUntilChanged()
    }

    private fun fetchCurrentUser(userId: String): Completable {
        return userService.whoami()
                .doOnSuccess { user ->
                    if (user.id != userId) {
                        throw RuntimeException("User changed unexpectedly")
                    }
                    persistWithTimestamp(user)
                }
                .ignoreElement()
    }

    override fun persistWithTimestamp(user: UserDto) {
        database.runInTransaction {
            val cacheEntry = CacheEntry(key = getCacheKey(user.id), timestamp = timeProvider.currentInstant)
            cacheEntryRepository.setEntry(cacheEntry)
            userDao.insertOrUpdate(user.toUserEntity())
        }
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

    private fun getCacheKey(userId: String) = "user-$userId"

    companion object {
        private val CACHE_DURATION = Duration.ofHours(4)
    }
}

private fun UserDto.toUserEntity(): UserEntity =
        UserEntity(id = id, name = name, type = type, avatarUrl = avatarUrl)
