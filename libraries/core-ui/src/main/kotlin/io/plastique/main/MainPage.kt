package io.plastique.main

import androidx.annotation.StringRes

import io.plastique.core.ExpandableToolbarLayout

interface MainPage {
    @StringRes
    fun getTitle(): Int

    fun createAppBarViews(parent: ExpandableToolbarLayout)
}
