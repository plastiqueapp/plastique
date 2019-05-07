package io.plastique.feed.settings

import com.sch.neon.Effect

sealed class FeedSettingsEffect : Effect() {
    object LoadFeedSettingsEffect : FeedSettingsEffect()
}
