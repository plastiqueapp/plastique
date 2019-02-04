package io.plastique.main

import android.content.Context
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory

interface MainFragmentFactory {
    fun createFragment(context: Context, fragmentFactory: FragmentFactory, @IdRes itemId: Int): Fragment
}
