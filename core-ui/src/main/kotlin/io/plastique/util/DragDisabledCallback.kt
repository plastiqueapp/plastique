package io.plastique.util

import com.google.android.material.appbar.AppBarLayout

object DragDisabledCallback : AppBarLayout.Behavior.DragCallback() {
    override fun canDrag(appBarLayout: AppBarLayout): Boolean = false
}
