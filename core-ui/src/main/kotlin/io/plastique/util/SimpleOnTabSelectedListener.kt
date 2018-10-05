package io.plastique.util

import com.google.android.material.tabs.TabLayout

abstract class SimpleOnTabSelectedListener : TabLayout.OnTabSelectedListener {
    /**
     * {@inheritDoc}
     */
    override fun onTabSelected(tab: TabLayout.Tab) {
    }

    /**
     * {@inheritDoc}
     */
    override fun onTabUnselected(tab: TabLayout.Tab) {
    }

    /**
     * {@inheritDoc}
     */
    override fun onTabReselected(tab: TabLayout.Tab) {
    }
}
