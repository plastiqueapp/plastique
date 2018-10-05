package io.plastique.core.extensions

import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

fun <T : CoordinatorLayout.Behavior<*>> View.getLayoutBehavior(): T {
    val layoutParams = layoutParams as CoordinatorLayout.LayoutParams
    @Suppress("UNCHECKED_CAST")
    return layoutParams.behavior as T
}

private var METHOD_INVALIDATE_SCROLL_RANGES: Method? = null

var View.isVisible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }

fun AppBarLayout.invalidateScrollRanges() {
    if (METHOD_INVALIDATE_SCROLL_RANGES == null) {
        try {
            METHOD_INVALIDATE_SCROLL_RANGES = AppBarLayout::class.java.getDeclaredMethod("invalidateScrollRanges")
            METHOD_INVALIDATE_SCROLL_RANGES!!.isAccessible = true
        } catch (e: NoSuchMethodException) {
            throw RuntimeException(e)
        }
    }
    try {
        METHOD_INVALIDATE_SCROLL_RANGES!!.invoke(this)
    } catch (e: IllegalAccessException) {
        throw RuntimeException(e)
    } catch (e: InvocationTargetException) {
        throw RuntimeException(e)
    }
}

fun RecyclerView.smartScrollToPosition(position: Int, maxSmoothScrollItemCount: Int) {
    val layoutManager = layoutManager as LinearLayoutManager
    val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
    val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

    val distance = when {
        position <= firstVisiblePosition -> firstVisiblePosition - position
        position >= lastVisiblePosition -> position - lastVisiblePosition
        else -> return
    }

    if (distance > maxSmoothScrollItemCount) {
        scrollToPosition(position)
    } else {
        smoothScrollToPosition(position)
    }
}

fun Toolbar.setTitleOnClickListener(onClickListener: View.OnClickListener) {
    try {
        val titleField = Toolbar::class.java.getDeclaredField("mTitleTextView")
        titleField.isAccessible = true

        var titleView = titleField.get(this) as View?
        if (titleView == null) {
            val title = title
            this.title = " " // Force Toolbar to create mTitleTextView
            this.title = title
            titleView = titleField.get(this) as View
        }
        titleView.setOnClickListener(onClickListener)
    } catch (e: NoSuchFieldException) {
        throw RuntimeException(e)
    } catch (e: IllegalAccessException) {
        throw RuntimeException(e)
    }
}

fun Toolbar.setSubtitleOnClickListener(onClickListener: View.OnClickListener) {
    try {
        val subtitleField = Toolbar::class.java.getDeclaredField("mSubtitleTextView")
        subtitleField.isAccessible = true

        var subtitleView = subtitleField.get(this) as View?
        if (subtitleView == null) {
            val subtitle = subtitle
            this.subtitle = " " // Force Toolbar to create mSubtitleTextView
            this.subtitle = subtitle
            subtitleView = subtitleField.get(this) as View
        }
        subtitleView.setOnClickListener(onClickListener)
    } catch (e: NoSuchFieldException) {
        throw RuntimeException(e)
    } catch (e: IllegalAccessException) {
        throw RuntimeException(e)
    }
}
