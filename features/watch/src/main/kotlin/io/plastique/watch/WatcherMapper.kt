package io.plastique.watch

import javax.inject.Inject

class WatcherMapper @Inject constructor() {
    fun map(watcherWithUser: WatcherWithUser): Watcher {
        val user = watcherWithUser.users.first()
        return Watcher(username = user.name, avatarUrl = user.avatarUrl)
    }
}
