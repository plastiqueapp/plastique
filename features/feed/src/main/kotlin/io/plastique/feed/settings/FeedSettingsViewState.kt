package io.plastique.feed.settings

import io.plastique.core.content.ContentState

data class FeedSettingsViewState(
    val contentState: ContentState,
    val settings: FeedSettings? = null,
    val items: List<OptionItem> = emptyList()
) {
    override fun toString(): String {
        return "FeedSettingsViewState(" +
                "contentState=$contentState, " +
                "settings=$settings, " +
                "items=${items.size}" +
                ")"
    }
}
