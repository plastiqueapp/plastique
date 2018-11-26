package io.plastique.core

import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class FragmentListPagerAdapter(
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val pages: List<Page>
) : BaseFragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        val className = pages[position].fragmentClass.name
        return fragmentManager.fragmentFactory.instantiate(context.classLoader, className, null)
    }

    override fun getCount(): Int = pages.size

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(pages[position].titleId)
    }

    data class Page(@StringRes val titleId: Int, val fragmentClass: Class<out Fragment>)
}
