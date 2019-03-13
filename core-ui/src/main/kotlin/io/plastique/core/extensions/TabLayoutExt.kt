package io.plastique.core.extensions

import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_DRAGGING
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_SETTLING
import com.google.android.material.tabs.TabLayout
import java.lang.ref.WeakReference

inline fun TabLayout.doOnTabReselected(crossinline action: (TabLayout.Tab) -> Unit) {
    addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
        }

        override fun onTabReselected(tab: TabLayout.Tab) {
            action(tab)
        }
    })
}

typealias OnConfigureTabListener = (tab: TabLayout.Tab, position: Int) -> Unit

fun TabLayout.setupWithViewPager(viewPager: ViewPager2, onConfigureTab: OnConfigureTabListener = { _, _ -> }) {
    val adapter: RecyclerView.Adapter<*> = viewPager.adapter
            ?: throw IllegalStateException("ViewPager2 has no adapter attached")

    val onPageChangeCallback = TabLayoutOnPageChangeCallback(this)
    viewPager.registerOnPageChangeCallback(onPageChangeCallback)

    val onTabSelectedListener = ViewPagerOnTabSelectedListener(viewPager)
    addOnTabSelectedListener(onTabSelectedListener)

    val pagerAdapterObserver = PagerAdapterObserver { populateTabsFromPagerAdapter(this, viewPager, adapter, onConfigureTab) }
    adapter.registerAdapterDataObserver(pagerAdapterObserver)

    populateTabsFromPagerAdapter(this, viewPager, adapter, onConfigureTab)

    // Now update the scroll position to match the ViewPager's current item
    setScrollPosition(viewPager.currentItem, 0f, true)
}

private fun populateTabsFromPagerAdapter(tabLayout: TabLayout, viewPager: ViewPager2, adapter: RecyclerView.Adapter<*>, onConfigureTab: OnConfigureTabListener) {
    tabLayout.removeAllTabs()

    val adapterCount = adapter.itemCount
    for (i in 0 until adapterCount) {
        val tab = tabLayout.newTab()
        onConfigureTab(tab, i)
        tabLayout.addTab(tab, false)
    }

    // Make sure we reflect the currently set ViewPager item
    if (adapterCount > 0) {
        val currentItem = viewPager.currentItem
        if (currentItem != tabLayout.selectedTabPosition) {
            tabLayout.getTabAt(currentItem)!!.select()
        }
    }
}

private class TabLayoutOnPageChangeCallback(tabLayout: TabLayout) : ViewPager2.OnPageChangeCallback() {
    private val tabLayoutRef = WeakReference(tabLayout)
    private var scrollState: Int = SCROLL_STATE_IDLE
    private var previousScrollState: Int = SCROLL_STATE_IDLE

    override fun onPageScrollStateChanged(state: Int) {
        previousScrollState = scrollState
        scrollState = state
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        val tabLayout = tabLayoutRef.get()
        if (tabLayout != null) {
            // Only update the text selection if we're not settling, or we are settling after
            // being dragged
            val updateText = scrollState != SCROLL_STATE_SETTLING || previousScrollState == SCROLL_STATE_DRAGGING
            // Update the indicator if we're not settling after being idle. This is caused
            // from a setCurrentItem() call and will be handled by an animation from
            // onPageSelected() instead.
            val updateIndicator = !(scrollState == SCROLL_STATE_SETTLING && previousScrollState == SCROLL_STATE_IDLE)
            tabLayout.setScrollPosition(position, positionOffset, updateText, updateIndicator)
        }
    }

    override fun onPageSelected(position: Int) {
        val tabLayout = tabLayoutRef.get()
        if (tabLayout != null && tabLayout.selectedTabPosition != position && position < tabLayout.tabCount) {
            // Select the tab, only updating the indicator if we're not being dragged/settled
            // (since onPageScrolled will handle that).
            val updateIndicator = scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_SETTLING && previousScrollState == SCROLL_STATE_IDLE
            tabLayout.selectTab(tabLayout.getTabAt(position), updateIndicator)
        }
    }
}

private class ViewPagerOnTabSelectedListener(private val viewPager: ViewPager2) : TabLayout.OnTabSelectedListener {
    override fun onTabSelected(tab: TabLayout.Tab) {
        viewPager.setCurrentItem(tab.position, true)
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
    }

    override fun onTabReselected(tab: TabLayout.Tab) {
    }
}

private class PagerAdapterObserver(private val onChangedListener: () -> Unit) : RecyclerView.AdapterDataObserver() {
    override fun onChanged() {
        onChangedListener()
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
        onChangedListener()
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
        onChangedListener()
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        onChangedListener()
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        onChangedListener()
    }

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        onChangedListener()
    }
}
