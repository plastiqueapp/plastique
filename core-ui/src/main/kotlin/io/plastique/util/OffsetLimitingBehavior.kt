package io.plastique.util

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Keep
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import kotlin.math.min

@Keep
class OffsetLimitingBehavior(context: Context, attrs: AttributeSet) : AppBarLayout.Behavior(context, attrs) {
    var maxOffset: Int = 0

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: AppBarLayout, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, clampDy(dy), consumed, type)
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: AppBarLayout, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, clampDy(dyUnconsumed), type)
    }

    private fun clampDy(dy: Int): Int {
        if (dy < 0 && maxOffset != 0) {
            val currentOffset = topAndBottomOffset
            val newOffset = min(currentOffset - dy, maxOffset)
            return min(currentOffset - newOffset, 0)
        }
        return dy
    }
}
