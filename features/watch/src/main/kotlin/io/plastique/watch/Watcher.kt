package io.plastique.watch

import io.plastique.users.User
import io.plastique.users.toUser

data class Watcher(
    val user: User
)

fun WatcherWithUser.toWatcher(): Watcher {
    return Watcher(user = users.first().toUser())
}
