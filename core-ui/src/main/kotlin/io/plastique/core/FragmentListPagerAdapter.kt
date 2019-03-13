package io.plastique.core

import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.collection.LongSparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.lang.reflect.Field

class FragmentListPagerAdapter(
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val pages: List<Page>
) : FragmentStateAdapter(fragmentManager) {

    @Suppress("UNCHECKED_CAST")
    private val fragments = FIELD_FRAGMENTS.get(this) as LongSparseArray<Fragment>

    override fun getItem(position: Int): Fragment {
        val page = pages[position]
        return fragmentManager.fragmentFactory.instantiate(context.classLoader, page.fragmentClass.name, page.args)
    }

    override fun getItemCount(): Int = pages.size

    fun getPageAt(position: Int): Page = pages[position]

    @Suppress("UNCHECKED_CAST")
    fun getFragmentAt(position: Int): Fragment? {
        val itemId = getItemId(position)
        return fragments[itemId]
    }

    data class Page(@StringRes val titleId: Int, val fragmentClass: Class<out Fragment>, val args: Bundle? = null)

    companion object {
        private val FIELD_FRAGMENTS: Field by lazy(LazyThreadSafetyMode.NONE) {
            FragmentStateAdapter::class.java.getDeclaredField("mFragments").apply { isAccessible = true }
        }
    }
}
