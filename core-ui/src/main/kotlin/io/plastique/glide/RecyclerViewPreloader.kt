package io.plastique.glide

import android.widget.AbsListView
import android.widget.AbsListView.OnScrollListener
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestManager
import io.plastique.core.extensions.findFirstVisibleItemPosition
import io.plastique.core.extensions.findLastVisibleItemPosition
import kotlin.math.abs

class RecyclerViewPreloader<T>(
    requestManager: RequestManager,
    preloadModelProvider: ListPreloader.PreloadModelProvider<T>,
    preloadDimensionProvider: ListPreloader.PreloadSizeProvider<T>,
    maxPreload: Int
) : RecyclerView.OnScrollListener() {

    var isEnabled: Boolean = true

    private val listPreloader = ListPreloader(requestManager, preloadModelProvider, preloadDimensionProvider, maxPreload)
    private val recyclerScrollListener = RecyclerToListViewScrollListener(listPreloader)

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (isEnabled) {
            recyclerScrollListener.onScrolled(recyclerView, dx, dy)
        }
    }
}

private class RecyclerToListViewScrollListener(private val scrollListener: AbsListView.OnScrollListener) : RecyclerView.OnScrollListener() {
    private var lastFirstVisiblePosition = RecyclerView.NO_POSITION
    private var lastVisibleCount = -1
    private var lastItemCount = -1

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        val listViewScrollState = when (newState) {
            RecyclerView.SCROLL_STATE_DRAGGING -> OnScrollListener.SCROLL_STATE_TOUCH_SCROLL
            RecyclerView.SCROLL_STATE_IDLE -> OnScrollListener.SCROLL_STATE_IDLE
            RecyclerView.SCROLL_STATE_SETTLING -> OnScrollListener.SCROLL_STATE_FLING
            else -> throw IllegalArgumentException("Unknown scroll state $newState")
        }

        scrollListener.onScrollStateChanged(null, listViewScrollState)
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val layoutManager = recyclerView.layoutManager ?: return
        val adapter = recyclerView.adapter ?: return

        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
        val visibleCount = abs(firstVisiblePosition - lastVisiblePosition)
        val itemCount = adapter.itemCount

        if (firstVisiblePosition != lastFirstVisiblePosition || visibleCount != lastVisibleCount || itemCount != lastItemCount) {
            scrollListener.onScroll(null, firstVisiblePosition, visibleCount, itemCount)
            lastFirstVisiblePosition = firstVisiblePosition
            lastVisibleCount = visibleCount
            lastItemCount = itemCount
        }
    }
}
