package io.plastique.watch

import io.plastique.core.lists.ListItem

data class WatcherItem(val watcher: Watcher) : ListItem {
    override val id: String get() = watcher.user.id
}
