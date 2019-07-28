package io.plastique.feed

import com.sch.neon.Effect
import io.plastique.feed.settings.FeedSettings

sealed class FeedEffect : Effect() {
    data class LoadFeedEffect(val matureContent: Boolean) : FeedEffect()
    object LoadMoreEffect : FeedEffect()
    object RefreshEffect : FeedEffect()
    object OpenSignInEffect : FeedEffect()

    data class SetFeedSettingsEffect(val settings: FeedSettings) : FeedEffect()
    data class SetFavoriteEffect(val deviationId: String, val favorite: Boolean) : FeedEffect()
}
