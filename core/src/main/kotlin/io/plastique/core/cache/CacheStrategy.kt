package io.plastique.core.cache

import io.reactivex.Completable
import io.reactivex.Observable
import timber.log.Timber

interface CacheStrategy {
    fun <T> apply(cacheStatus: CacheStatus, data: Observable<T>, updater: Completable): Observable<T>
}

object UpdateIfNotActualStrategy : CacheStrategy {
    override fun <T> apply(cacheStatus: CacheStatus, data: Observable<T>, updater: Completable): Observable<T> = when (cacheStatus) {
        CacheStatus.Actual -> data
        CacheStatus.Outdated -> data.mergeWith(updater
                .doOnError(Timber::e)
                .onErrorComplete()) // TODO: Distinguish non-fatal and fatal errors, e.g. when the requested entity was deleted.
        CacheStatus.Absent -> updater.andThen(data)
    }
}

object UpdateIfAbsentStrategy : CacheStrategy {
    override fun <T> apply(cacheStatus: CacheStatus, data: Observable<T>, updater: Completable): Observable<T> = when (cacheStatus) {
        CacheStatus.Actual, CacheStatus.Outdated -> data
        CacheStatus.Absent -> updater.andThen(data)
    }
}
