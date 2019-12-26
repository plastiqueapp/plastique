package io.plastique.core.cache

import io.reactivex.Completable
import io.reactivex.Observable
import timber.log.Timber

class CacheHelper(
    private val cacheEntryRepository: CacheEntryRepository,
    private val cacheEntryChecker: CacheEntryChecker,
    private val cacheStrategy: CacheStrategy = UpdateIfNotActualStrategy
) {
    fun <T> createObservable(cacheKey: CacheKey, cachedData: Observable<T>, updater: Completable): Observable<T> {
        return Observable.defer {
            val cacheStatus = getCacheStatus(cacheKey)
            Timber.tag(LOG_TAG).d("Cache status for '%s': %s", cacheKey.value, cacheStatus)
            cacheStrategy.apply(cacheStatus, cachedData, updater)
        }
    }

    private fun getCacheStatus(cacheKey: CacheKey): CacheStatus {
        val cacheEntry = cacheEntryRepository.getEntryByKey(cacheKey)
        return cacheEntry?.let(cacheEntryChecker::getCacheStatus) ?: CacheStatus.Absent
    }

    companion object {
        private const val LOG_TAG = "CacheHelper"
    }
}
