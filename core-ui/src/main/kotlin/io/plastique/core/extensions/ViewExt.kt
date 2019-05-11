package io.plastique.core.extensions

import android.graphics.Paint
import android.view.View
import android.view.ViewParent
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.appbar.AppBarLayout
import java.lang.reflect.Field
import java.lang.reflect.Method

fun <T : CoordinatorLayout.Behavior<*>> View.getLayoutBehavior(): T {
    val layoutParams = layoutParams as CoordinatorLayout.LayoutParams
    @Suppress("UNCHECKED_CAST")
    return layoutParams.behavior as T
}

fun AppBarLayout.disableDragging() {
    val layoutParams = layoutParams as CoordinatorLayout.LayoutParams
    val behavior = layoutParams.behavior as AppBarLayout.Behavior?
    if (behavior != null) {
        behavior.setDragCallback(DisabledDragCallback)
    } else {
        doOnLayout {
            (layoutParams.behavior as AppBarLayout.Behavior).setDragCallback(DisabledDragCallback)
        }
    }
}

fun AppBarLayout.invalidateScrollRanges() {
    METHOD_APPBAR_INVALIDATE_SCROLL_RANGES(this)
}

fun RecyclerView.smartScrollToPosition(position: Int, maxSmoothScrollItemCount: Int = 10) {
    val layoutManager = this.layoutManager ?: return
    val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
    val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
    if (firstVisiblePosition == RecyclerView.NO_POSITION || lastVisiblePosition == RecyclerView.NO_POSITION) {
        return
    }

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

fun RecyclerView.LayoutManager.findFirstVisibleItemPosition(): Int = when (this) {
    is FlexboxLayoutManager -> findFirstVisibleItemPosition()
    is LinearLayoutManager -> findFirstVisibleItemPosition()
    else -> throw UnsupportedOperationException("Unsupported layout manager $javaClass")
}

fun RecyclerView.LayoutManager.findLastVisibleItemPosition(): Int = when (this) {
    is FlexboxLayoutManager -> findLastVisibleItemPosition()
    is LinearLayoutManager -> findLastVisibleItemPosition()
    else -> throw UnsupportedOperationException("Unsupported layout manager $javaClass")
}

var TextView.isStrikethrough: Boolean
    get() = (paintFlags and Paint.STRIKE_THRU_TEXT_FLAG) == Paint.STRIKE_THRU_TEXT_FLAG
    set(value) {
        paintFlags = if (value) {
            paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

fun Toolbar.setTitleOnClickListener(onClickListener: View.OnClickListener) {
    var titleView = FIELD_TOOLBAR_TITLE.get(this) as View?
    if (titleView == null) {
        val title = title
        this.title = " " // Force Toolbar to create mTitleTextView
        this.title = title
        titleView = FIELD_TOOLBAR_TITLE.get(this) as View
    }
    titleView.setOnClickListener(onClickListener)
}

fun Toolbar.setSubtitleOnClickListener(onClickListener: View.OnClickListener) {
    var subtitleView = FIELD_TOOLBAR_SUBTITLE.get(this) as View?
    if (subtitleView == null) {
        val subtitle = subtitle
        this.subtitle = " " // Force Toolbar to create mSubtitleTextView
        this.subtitle = subtitle
        subtitleView = FIELD_TOOLBAR_SUBTITLE.get(this) as View
    }
    subtitleView.setOnClickListener(onClickListener)
}

fun <T : ViewParent> View.findParentOfType(type: Class<T>): T? {
    var p = parent
    while (p != null) {
        if (type.isInstance(p)) {
            return type.cast(p)
        }
        p = p.parent
    }
    return null
}

private object DisabledDragCallback : AppBarLayout.Behavior.DragCallback() {
    override fun canDrag(appBarLayout: AppBarLayout): Boolean = false
}

private val FIELD_TOOLBAR_TITLE: Field by lazy(LazyThreadSafetyMode.NONE) {
    Toolbar::class.java.getDeclaredField("mTitleTextView").apply { isAccessible = true }
}

private val FIELD_TOOLBAR_SUBTITLE: Field by lazy(LazyThreadSafetyMode.NONE) {
    Toolbar::class.java.getDeclaredField("mSubtitleTextView").apply { isAccessible = true }
}

private val METHOD_APPBAR_INVALIDATE_SCROLL_RANGES: Method by lazy(LazyThreadSafetyMode.NONE) {
    AppBarLayout::class.java.getDeclaredMethod("invalidateScrollRanges").apply { isAccessible = true }
}
