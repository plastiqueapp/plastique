package io.plastique.watch

import io.plastique.api.watch.WatchService
import io.plastique.users.UserRepository
import io.reactivex.Completable
import javax.inject.Inject

class WatchManagerImpl @Inject constructor(
    private val watchService: WatchService,
    private val userRepository: UserRepository
) : WatchManager {
    override fun setWatching(username: String, watching: Boolean): Completable {
        return if (watching) {
            val params = DEFAULT_WATCH_TYPES.mapKeys { "watch[${it.key}]" }
            watchService.watch(username, params)
        } else {
            watchService.unwatch(username)
        }.doOnComplete {
            userRepository.setWatching(username, watching)
        }
    }

    companion object {
        private val DEFAULT_WATCH_TYPES = mapOf(
            "friend" to false,
            "deviations" to true,
            "collections" to true,
            "journals" to true,
            "forum_threads" to true,
            "critiques" to true,
            "scraps" to true,
            "activity" to true)
    }
}
