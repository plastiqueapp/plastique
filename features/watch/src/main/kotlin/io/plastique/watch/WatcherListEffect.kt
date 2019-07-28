package io.plastique.watch

import com.sch.neon.Effect

sealed class WatcherListEffect : Effect() {
    data class LoadWatchersEffect(val username: String?) : WatcherListEffect()
    object LoadMoreEffect : WatcherListEffect()
    object RefreshEffect : WatcherListEffect()
    object OpenSignInEffect : WatcherListEffect()
}
