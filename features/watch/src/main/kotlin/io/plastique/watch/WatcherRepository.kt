package io.plastique.watch

import androidx.room.RoomDatabase
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.watch.WatchService
import io.plastique.core.cache.CacheEntry
import io.plastique.core.cache.CacheEntryRepository
import io.plastique.core.cache.CacheHelper
import io.plastique.core.cache.DurationBasedCacheEntryChecker
import io.plastique.core.converters.NullFallbackConverter
import io.plastique.core.paging.OffsetCursor
import io.plastique.core.paging.PagedData
import io.plastique.core.paging.nextCursor
import io.plastique.core.session.SessionManager
import io.plastique.core.session.currentUsername
import io.plastique.users.UserRepository
import io.plastique.users.toUserEntity
import io.plastique.util.RxRoom
import io.plastique.util.TimeProvider
import io.reactivex.Completable
import io.reactivex.Observable
import org.threeten.bp.Duration
import java.util.concurrent.Callable
import javax.inject.Inject
import io.plastique.api.watch.Watcher as WatcherDto

class WatcherRepository @Inject constructor(
    private val database: RoomDatabase,
    private val watchService: WatchService,
    private val cacheEntryRepository: CacheEntryRepository,
    private val metadataConverter: NullFallbackConverter,
    private val sessionManager: SessionManager,
    private val timeProvider: TimeProvider,
    private val userRepository: UserRepository,
    private val watchDao: WatchDao
) {
    private val cacheHelper = CacheHelper(cacheEntryRepository, DurationBasedCacheEntryChecker(timeProvider, CACHE_DURATION))

    fun getWatchers(username: String?): Observable<PagedData<List<Watcher>, OffsetCursor>> {
        return Observable.defer {
            val cacheKey = getCacheKey(username ?: sessionManager.currentUsername)
            cacheHelper.createObservable(
                    cacheKey = cacheKey,
                    cachedData = getWatchersFromDb(cacheKey),
                    updater = fetchWatchers(username, null))
        }
    }

    fun fetchWatchers(username: String?, cursor: OffsetCursor? = null): Completable {
        return Completable.defer {
            val cacheKey = getCacheKey(username ?: sessionManager.currentUsername)
            fetchWatchers(username, cursor, cacheKey)
        }
    }

    private fun fetchWatchers(username: String?, cursor: OffsetCursor?, cacheKey: String): Completable {
        val offset = cursor?.offset ?: 0
        return if (username != null) {
            watchService.getWatchers(username, offset, WATCHERS_PER_PAGE)
        } else {
            watchService.getWatchers(offset, WATCHERS_PER_PAGE)
        }
                .doOnSuccess { watcherList ->
                    val cacheMetadata = WatchersCacheMetadata(nextCursor = watcherList.nextCursor)
                    val cacheEntry = CacheEntry(cacheKey, timeProvider.currentInstant, metadataConverter.toJson(cacheMetadata))
                    persist(watchers = watcherList.results, cacheEntry = cacheEntry, replaceExisting = offset == 0)
                }
                .ignoreElement()
    }

    private fun getWatchersFromDb(cacheKey: String): Observable<PagedData<List<Watcher>, OffsetCursor>> {
        return RxRoom.createObservable(database, arrayOf("watchers")) {
            database.runInTransaction(Callable {
                val watchers = watchDao.getWatchers(cacheKey).map { watcherWithUser -> watcherWithUser.toWatcher() }
                val nextCursor = getNextCursor(cacheKey)
                PagedData(watchers, nextCursor)
            })
        }.distinctUntilChanged()
    }

    private fun persist(cacheEntry: CacheEntry, watchers: List<WatcherDto>, replaceExisting: Boolean) {
        val users = watchers.map { watcher -> watcher.user.toUserEntity() }

        database.runInTransaction {
            userRepository.put(users)
            cacheEntryRepository.setEntry(cacheEntry)

            var order = if (replaceExisting) {
                watchDao.deleteWatchersByKey(cacheEntry.key)
                1
            } else {
                watchDao.getMaxOrder(cacheEntry.key) + 1
            }

            val watcherEntities = watchers.map { watcher -> WatcherEntity(key = cacheEntry.key, userId = watcher.user.id, order = order++) }
            watchDao.insertWatchers(watcherEntities) // TODO: Handle possible duplicates
        }
    }

    private fun getNextCursor(cacheKey: String): OffsetCursor? {
        val cacheEntry = cacheEntryRepository.getEntryByKey(cacheKey)
        val metadata = cacheEntry?.metadata?.let { metadataConverter.fromJson<WatchersCacheMetadata>(it) }
        return metadata?.nextCursor
    }

    private fun getCacheKey(username: String): String = "watchers-$username"

    private companion object {
        private val CACHE_DURATION = Duration.ofHours(4)
        private const val WATCHERS_PER_PAGE = 50
    }
}

@JsonClass(generateAdapter = true)
data class WatchersCacheMetadata(
    @Json(name = "next_cursor")
    val nextCursor: OffsetCursor? = null
)
