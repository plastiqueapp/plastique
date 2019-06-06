package io.plastique.core.extensions

import com.google.android.material.tabs.TabLayout

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
