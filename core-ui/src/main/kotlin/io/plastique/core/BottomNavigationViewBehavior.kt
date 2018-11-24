package io.plastique.core

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.annotation.Keep
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar

@Keep
class BottomNavigationViewBehavior(context: Context, attrs: AttributeSet?) : HideBottomViewOnScrollBehavior<BottomNavigationView>(context, attrs) {
    override fun layoutDependsOn(parent: CoordinatorLayout, child: BottomNavigationView, dependency: View): Boolean {
        if (dependency is Snackbar.SnackbarLayout) {
            (dependency.layoutParams as CoordinatorLayout.LayoutParams).run {
                anchorId = child.id
                anchorGravity = Gravity.TOP
                gravity = Gravity.TOP
            }
        }
        return super.layoutDependsOn(parent, child, dependency)
    }
}
