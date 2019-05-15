package io.plastique.core.cache

import android.annotation.SuppressLint
import io.plastique.core.session.OnLogoutListener
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

class CacheCleaner @Inject constructor(
    private val cleanableRepository: Provider<Set<CleanableRepository>>
) : OnLogoutListener {

    @SuppressLint("CheckResult")
    override fun onLogout() {
        cleanableRepository.get().toObservable()
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
