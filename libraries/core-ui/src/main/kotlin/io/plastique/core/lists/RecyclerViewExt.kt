package io.plastique.core.lists

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager

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
