package io.plastique.main

import android.content.Context
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.github.technoir42.android.extensions.instantiate
import io.plastique.R
import io.plastique.collections.CollectionsFragment
import io.plastique.deviations.BrowseDeviationsFragment
import io.plastique.feed.FeedFragment
import io.plastique.gallery.GalleryFragment
import io.plastique.notifications.NotificationsFragment
import javax.inject.Inject

class MainFragmentFactoryImpl @Inject constructor() : MainFragmentFactory {
    override fun createFragment(context: Context, fragmentFactory: FragmentFactory, @IdRes itemId: Int): Fragment = when (itemId) {
        R.id.main_tab_browse -> fragmentFactory.instantiate<BrowseDeviationsFragment>(context)
        R.id.main_tab_favorites -> fragmentFactory.instantiate<CollectionsFragment>(context, args = CollectionsFragment.newArgs())
        R.id.main_tab_gallery -> fragmentFactory.instantiate<GalleryFragment>(context, args = GalleryFragment.newArgs())
        R.id.main_tab_notifications -> fragmentFactory.instantiate<NotificationsFragment>(context)
        R.id.main_tab_watch -> fragmentFactory.instantiate<FeedFragment>(context)
        else -> throw IllegalArgumentException("Unhandled itemId $itemId")
    }
}
