package io.plastique

import android.annotation.SuppressLint
import dagger.Lazy
import io.plastique.core.session.OnLogoutListener
import io.plastique.feed.FeedRepository
import io.plastique.notifications.MessageRepository
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class DatabaseCleaner @Inject constructor(
    private val feedRepository: Lazy<FeedRepository>,
    private val messageRepository: Lazy<MessageRepository>
) : OnLogoutListener {

    @SuppressLint("CheckResult")
    override fun onLogout() {
        Completable.concatArray(feedRepository.get().clearCache(), messageRepository.get().clearCache())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    Timber.tag(LOG_TAG).d("Cleanup complete")
                }, { error ->
                    Timber.tag(LOG_TAG).e(error)
                })
    }

    companion object {
        private const val LOG_TAG = "DatabaseCleaner"
    }
}
