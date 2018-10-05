package io.plastique.core

import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class FragmentListPagerAdapter(
    private val context: Context,
    fragmentManager: FragmentManager,
    private vararg val pages: Page
) : BaseFragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return Fragment.instantiate(context, pages[position].fragmentClass.name)
    }

    override fun getCount(): Int = pages.size

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(pages[position].titleId)
    }

    data class Page(@StringRes val titleId: Int, val fragmentClass: Class<out Fragment>)
}
