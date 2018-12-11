package io.plastique.feed.settings

import io.plastique.core.flow.Effect

sealed class FeedSettingsEffect : Effect() {
    object LoadFeedSettingsEffect : FeedSettingsEffect()
}
