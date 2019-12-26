package io.plastique.users

import androidx.room.RoomDatabase
import io.plastique.api.users.UserDto
import io.plastique.api.users.UserService
import io.plastique.core.cache.CacheEntry
import io.plastique.core.cache.CacheEntryRepository
import io.plastique.core.cache.CacheHelper
import io.plastique.core.cache.CacheKey
import io.plastique.core.cache.DurationBasedCacheEntryChecker
import io.plastique.core.cache.toCacheKey
import io.reactivex.Completable
import io.reactivex.Observable
import org.threeten.bp.Clock
import org.threeten.bp.Duration
import javax.inject.Inject
import kotlin.math.max

class UserRepositoryImpl @Inject constructor(
    private val clock: Clock,
    private val database: RoomDatabase,
    private val userDao: UserDao,
    private val userService: UserService,
    private val cacheEntryRepository: CacheEntryRepository
) : UserRepository {

    override fun getCurrentUser(userId: String): Observable<User> {
        val cacheHelper = CacheHelper(cacheEntryRepository, DurationBasedCacheEntryChecker(clock, CACHE_DURATION))
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
                check(user.id == userId) { "User changed unexpectedly" }
                persistWithTimestamp(user)
            }
            .ignoreElement()
    }

    override fun persistWithTimestamp(user: UserDto) {
        database.runInTransaction {
            val cacheEntry = CacheEntry(key = getCacheKey(user.id), timestamp = clock.instant())
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
        database.runInTransaction {
            val watchInfo = userDao.getWatchInfo(username)
            if (watchInfo != null && watchInfo.isWatching != watching) {
                val newWatcherCount = if (watching) watchInfo.watcherCount + 1 else max(watchInfo.watcherCount - 1, 0)
                userDao.setWatching(username, watching, newWatcherCount)
            }
        }
    }

    private fun getCacheKey(userId: String): CacheKey =
        "user-$userId".toCacheKey()

    companion object {
        private val CACHE_DURATION = Duration.ofHours(4)
    }
}

private fun UserDto.toUserEntity(): UserEntity =
    UserEntity(id = id, name = name, type = type, avatarUrl = avatarUrl)
