package io.plastique.main

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment

interface MainFragmentFactory {
    fun createFragment(@IdRes itemId: Int): Fragment
}
