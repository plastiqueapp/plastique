package io.plastique.users.profile

import androidx.room.RoomDatabase
import com.sch.rxjava2.extensions.mapError
import io.plastique.api.common.ErrorType
import io.plastique.api.users.UserProfileDto
import io.plastique.api.users.UserService
import io.plastique.core.cache.CacheEntry
import io.plastique.core.cache.CacheEntryRepository
import io.plastique.core.cache.CacheHelper
import io.plastique.core.cache.DurationBasedCacheEntryChecker
import io.plastique.core.exceptions.ApiException
import io.plastique.core.exceptions.UserNotFoundException
import io.plastique.core.extensions.nullIfEmpty
import io.plastique.users.UserDao
import io.plastique.users.UserRepository
import io.plastique.users.toUser
import io.plastique.util.TimeProvider
import io.reactivex.Completable
import io.reactivex.Observable
import org.threeten.bp.Duration
import javax.inject.Inject

class UserProfileRepository @Inject constructor(
    private val database: RoomDatabase,
    private val userDao: UserDao,
    private val userService: UserService,
    private val cacheEntryRepository: CacheEntryRepository,
    private val userRepository: UserRepository,
    private val timeProvider: TimeProvider
) {
    private val cacheHelper = CacheHelper(cacheEntryRepository, DurationBasedCacheEntryChecker(timeProvider, CACHE_DURATION))

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

    private fun refreshUserProfile(username: String, cacheKey: String): Completable {
        return userService.getUserProfile(username)
                .doOnSuccess { userProfile ->
                    val cacheEntry = CacheEntry(cacheKey, timeProvider.currentInstant)
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

    private fun deleteUser(username: String, cacheKey: String) {
        database.runInTransaction {
            userDao.deleteProfileByName(username)
            cacheEntryRepository.deleteEntryByKey(cacheKey)
        }
    }

    private fun getCacheKey(username: String): String {
        return "user-profile-$username"
    }

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
        isWatching = isWatching)

private fun UserProfileEntityWithRelations.toUserProfile(): UserProfile = UserProfile(
        user = users.first().toUser(),
        url = userProfile.url,
        realName = userProfile.realName,
        bio = userProfile.bio,
        isWatching = userProfile.isWatching)
