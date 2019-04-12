package io.plastique.core

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.collection.LongSparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentFactory
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.lang.reflect.Field

class FragmentListPagerAdapter : FragmentStateAdapter {
    private val fragmentFactory: FragmentFactory
    private val classLoader: ClassLoader
    private val pages: List<Page>
    @Suppress("UNCHECKED_CAST")
    private val fragments = FIELD_FRAGMENTS.get(this) as LongSparseArray<Fragment>

    constructor(activity: FragmentActivity, pages: List<Page>) : super(activity) {
        this.fragmentFactory = activity.supportFragmentManager.fragmentFactory
        this.classLoader = activity.classLoader
        this.pages = pages
    }

    constructor(fragment: Fragment, pages: List<Page>) : super(fragment) {
        this.fragmentFactory = fragment.childFragmentManager.fragmentFactory
        this.classLoader = fragment.requireContext().classLoader
        this.pages = pages
    }

    override fun getItem(position: Int): Fragment {
        val page = pages[position]
        return fragmentFactory.instantiate(classLoader, page.fragmentClass.name).apply { arguments = page.args }
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
