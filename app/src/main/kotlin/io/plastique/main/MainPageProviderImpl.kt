package io.plastique.main

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import io.plastique.R
import io.plastique.collections.CollectionsFragment
import io.plastique.deviations.browse.BrowseDeviationsFragment
import io.plastique.feed.FeedFragment
import io.plastique.gallery.GalleryFragment
import io.plastique.notifications.NotificationsFragment
import javax.inject.Inject

class MainPageProviderImpl @Inject constructor() : MainPageProvider {
    override fun getPageFragmentClass(@IdRes itemId: Int): Class<out Fragment> = when (itemId) {
        R.id.main_tab_browse -> BrowseDeviationsFragment::class.java
        R.id.main_tab_favorites -> CollectionsFragment::class.java
        R.id.main_tab_gallery -> GalleryFragment::class.java
        R.id.main_tab_notifications -> NotificationsFragment::class.java
        R.id.main_tab_watch -> FeedFragment::class.java
        else -> throw IllegalArgumentException("Unhandled itemId $itemId")
    }
}
