package io.plastique.feed.settings

import io.plastique.core.content.EmptyState

sealed class FeedSettingsViewState {
    override fun toString(): String = "FeedSettingsViewState.${javaClass.simpleName}"

    data class Content(
        val settings: FeedSettings,
        val items: List<OptionItem>,
        val changedSettings: FeedSettings
    ) : FeedSettingsViewState() {
        override fun toString(): String {
            return "FeedSettingsViewState.Content(" +
                    "settings=$settings, " +
                    "items=${items.size}, " +
                    "changedSettings=$changedSettings" +
                    ")"
        }
    }

    data class Empty(val emptyState: EmptyState) : FeedSettingsViewState() {
        override fun toString(): String = "FeedSettingsViewState.Empty(emptyState=$emptyState)"
    }

    object Loading : FeedSettingsViewState()
}
