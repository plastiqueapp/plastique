package io.plastique.core.cache

import io.reactivex.Completable
import io.reactivex.Observable

class CacheHelper(
    private val cacheEntryRepository: CacheEntryRepository,
    private val cacheEntryChecker: CacheEntryChecker,
    private val cacheStrategy: CacheStrategy = UpdateIfNotActualStrategy
) {
    fun <T> createObservable(cacheKey: String, cachedData: Observable<T>, updater: Completable): Observable<T> {
        return Observable.defer { cacheStrategy.apply(getCacheStatus(cacheKey), cachedData, updater) }
    }

    private fun getCacheStatus(cacheKey: String): CacheStatus {
        val cacheEntry = cacheEntryRepository.getEntryByKey(cacheKey)
        return cacheEntry?.let(cacheEntryChecker::getCacheStatus) ?: CacheStatus.Absent
    }
}
