package io.plastique.watch

data class Watcher(
    val username: String,
    val avatarUrl: String?
)

fun WatcherWithUser.toWatcher(): Watcher {
    val user = users.first()
    return Watcher(username = user.name, avatarUrl = user.avatarUrl)
}
