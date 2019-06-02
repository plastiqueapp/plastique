package io.plastique.glide

import android.widget.AbsListView.OnScrollListener
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import io.plastique.core.extensions.findFirstVisibleItemPosition
import io.plastique.core.extensions.findLastVisibleItemPosition
import io.plastique.util.Size
import kotlin.math.abs

class RecyclerViewPreloader<T>(
    requestManager: RequestManager,
    lifecycle: Lifecycle,
    callback: Callback<T>,
    maxPreload: Int
) : RecyclerView.OnScrollListener() {

    private val recyclerScrollListener: RecyclerView.OnScrollListener
    private var isEnabled: Boolean = false

    init {
        val preloadModelProvider = object : ListPreloader.PreloadModelProvider<T> {
            override fun getPreloadItems(position: Int): List<T?> = callback.getPreloadItems(position)

            override fun getPreloadRequestBuilder(item: T): RequestBuilder<*> = callback.createRequestBuilder(item)
        }

        val preloadSizeProvider = ListPreloader.PreloadSizeProvider<T> { item, _, _ ->
            val size = callback.getPreloadSize(item)
            intArrayOf(size.width, size.height)
        }

        val listPreloader = ListPreloader(requestManager, preloadModelProvider, preloadSizeProvider, maxPreload)
        recyclerScrollListener = RecyclerToListViewScrollListener(listPreloader)

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                isEnabled = true
            }

            override fun onPause(owner: LifecycleOwner) {
                isEnabled = false
            }

            override fun onDestroy(owner: LifecycleOwner) {
                lifecycle.removeObserver(this)
            }
        })
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (isEnabled) {
            recyclerScrollListener.onScrolled(recyclerView, dx, dy)
        }
    }

    interface Callback<T> {
        fun getPreloadItems(position: Int): List<T>

        fun createRequestBuilder(item: T): RequestBuilder<*>

        fun getPreloadSize(item: T): Size
    }
}

private class RecyclerToListViewScrollListener(private val scrollListener: OnScrollListener) : RecyclerView.OnScrollListener() {
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
