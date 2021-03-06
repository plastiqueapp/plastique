package io.plastique.users.profile

import androidx.room.RoomDatabase
import com.github.technoir42.kotlin.extensions.nullIfEmpty
import com.github.technoir42.rxjava2.extensions.mapError
import io.plastique.api.ApiException
import io.plastique.api.common.ErrorType
import io.plastique.api.users.UserProfileDto
import io.plastique.api.users.UserService
import io.plastique.core.cache.CacheEntry
import io.plastique.core.cache.CacheEntryRepository
import io.plastique.core.cache.CacheHelper
import io.plastique.core.cache.CacheKey
import io.plastique.core.cache.DurationBasedCacheEntryChecker
import io.plastique.core.cache.toCacheKey
import io.plastique.users.UserDao
import io.plastique.users.UserNotFoundException
import io.plastique.users.UserRepository
import io.plastique.users.toUser
import io.reactivex.Completable
import io.reactivex.Observable
import org.threeten.bp.Clock
import org.threeten.bp.Duration
import javax.inject.Inject

class UserProfileRepository @Inject constructor(
    private val clock: Clock,
    private val database: RoomDatabase,
    private val userDao: UserDao,
    private val userService: UserService,
    private val cacheEntryRepository: CacheEntryRepository,
    private val userRepository: UserRepository
) {
    private val cacheHelper = CacheHelper(cacheEntryRepository, DurationBasedCacheEntryChecker(clock, CACHE_DURATION))

    fun getUserProfileByName(username: String): Observable<UserProfile> {
        val cacheKey = getCacheKey(username)
        return cacheHelper.createObservable(
            cacheKey = cacheKey,
            cachedData = getUserProfileFromDb(username),
            updater = refreshUserProfile(username, cacheKey))
    }

    private fun getUserProfileFromDb(username: String): Observable<UserProfile> {
        return userDao.getProfileByName(username)
            .map { userProfileWithUser -> userProfileWithUser.toUserProfile() }
            .distinctUntilChanged()
    }

    private fun refreshUserProfile(username: String, cacheKey: CacheKey): Completable {
        return userService.getUserProfile(username)
            .doOnSuccess { userProfile ->
                val cacheEntry = CacheEntry(key = cacheKey, timestamp = clock.instant())
                persistUserProfile(cacheEntry = cacheEntry, userProfile = userProfile)
            }
            .mapError { error ->
                if (error is ApiException && error.errorData.type == ErrorType.InvalidRequest && error.errorData.code == ERROR_CODE_USER_NOT_FOUND) {
                    deleteUser(username, cacheKey)
                    UserNotFoundException(username, error)
                } else {
                    error
                }
            }
            .ignoreElement()
    }

    private fun persistUserProfile(cacheEntry: CacheEntry, userProfile: UserProfileDto) {
        database.runInTransaction {
            userRepository.put(listOf(userProfile.user))
            userDao.insertOrUpdate(userProfile.toUserProfileEntity())
            cacheEntryRepository.setEntry(cacheEntry)
        }
    }

    private fun deleteUser(username: String, cacheKey: CacheKey) {
        database.runInTransaction {
            userDao.deleteProfileByName(username)
            cacheEntryRepository.deleteEntryByKey(cacheKey)
        }
    }

    private fun getCacheKey(username: String): CacheKey =
        "user-profile-$username".toCacheKey()

    companion object {
        private val CACHE_DURATION = Duration.ofHours(1)
        private const val ERROR_CODE_USER_NOT_FOUND = 2
    }
}

private fun UserProfileDto.toUserProfileEntity(): UserProfileEntity = UserProfileEntity(
    userId = user.id,
    url = url,
    realName = realName.nullIfEmpty(),
    bio = bio.nullIfEmpty(),
    isWatching = isWatching,
    stats = UserProfileEntity.Stats(
        userDeviations = stats.userDeviations,
        userFavorites = stats.userFavorites,
        watchers = user.stats!!.watchers))

private fun UserProfileEntityWithRelations.toUserProfile(): UserProfile = UserProfile(
    user = user.toUser(),
    url = userProfile.url,
    realName = userProfile.realName,
    bio = userProfile.bio,
    isWatching = userProfile.isWatching,
    stats = UserProfile.Stats(
        deviations = userProfile.stats.userDeviations,
        favorites = userProfile.stats.userFavorites,
        watchers = userProfile.stats.watchers))
