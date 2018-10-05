package io.plastique.core

import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

/**
 * [FragmentStatePagerAdapter] that keeps track of added fragments.
 */
abstract class BaseFragmentStatePagerAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager) {
    private val fragments = SparseArray<Fragment>()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as Fragment
        fragments.put(position, fragment)
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        super.destroyItem(container, position, item)
        fragments.remove(position)
    }

    fun getFragmentAtPosition(position: Int): Fragment? {
        return fragments.get(position)
    }
}
