package io.plastique.core.cache

import android.annotation.SuppressLint
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

class CacheCleaner @Inject constructor(
    private val cleanableRepository: Provider<Set<CleanableRepository>>
) {
    @SuppressLint("CheckResult")
    fun clean() {
        Observable.fromIterable(cleanableRepository.get())
            .concatMapCompletable { it.cleanCache() }
            .subscribeOn(Schedulers.io())
            .subscribe({
                Timber.tag(LOG_TAG).d("Cleanup complete")
            }, { error ->
                Timber.tag(LOG_TAG).e(error)
            })
    }

    companion object {
        private const val LOG_TAG = "CacheCleaner"
    }
}
