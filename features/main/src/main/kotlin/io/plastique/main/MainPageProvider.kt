package io.plastique.main

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment

interface MainPageProvider {
    fun getPageFragmentClass(@IdRes itemId: Int): Class<out Fragment>
}
