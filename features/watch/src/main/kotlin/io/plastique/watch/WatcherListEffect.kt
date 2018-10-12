package io.plastique.watch

import io.plastique.core.flow.Effect

sealed class WatcherListEffect : Effect() {
    data class LoadWatchersEffect(val username: String?) : WatcherListEffect()
    object LoadMoreEffect : WatcherListEffect()
    object RefreshEffect : WatcherListEffect()
}
