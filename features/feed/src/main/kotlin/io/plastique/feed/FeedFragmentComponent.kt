package io.plastique.feed

import io.plastique.feed.settings.FeedSettingsFragment

interface FeedFragmentComponent {
    fun inject(fragment: FeedFragment)

    fun inject(fragment: FeedSettingsFragment)
}
