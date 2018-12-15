package io.plastique.watch

import io.reactivex.Completable

interface WatchManager {
    fun setWatching(username: String, watching: Boolean): Completable
}
