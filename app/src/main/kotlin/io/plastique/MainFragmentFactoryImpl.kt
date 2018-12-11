package io.plastique

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import io.plastique.collections.CollectionsFragment
import io.plastique.deviations.BrowseDeviationsFragment
import io.plastique.feed.FeedFragment
import io.plastique.gallery.GalleryFragment
import io.plastique.main.MainFragmentFactory
import io.plastique.profile.ProfileFragment
import javax.inject.Inject

class MainFragmentFactoryImpl @Inject constructor() : MainFragmentFactory {
    override fun createFragment(@IdRes itemId: Int): Fragment = when (itemId) {
        R.id.main_tab_browse -> BrowseDeviationsFragment()
        R.id.main_tab_collections -> CollectionsFragment.newInstance()
        R.id.main_tab_gallery -> GalleryFragment.newInstance()
        R.id.main_tab_profile -> ProfileFragment()
        R.id.main_tab_watch -> FeedFragment()
        else -> throw IllegalArgumentException("Unhandled itemId $itemId")
    }
}
