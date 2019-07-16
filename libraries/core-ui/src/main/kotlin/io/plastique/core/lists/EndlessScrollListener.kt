package io.plastique.core.lists

import androidx.recyclerview.widget.RecyclerView

class EndlessScrollListener(
    var loadMoreThreshold: Int,
    var isEnabled: Boolean = false,
    private val onLoadMore: () -> Unit
) : RecyclerView.OnScrollListener() {
    private var scrollStateReset = true

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (!isEnabled) {
            return
        }
        val layoutManager = recyclerView.layoutManager ?: return
        val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
        val remainingItemCount = layoutManager.itemCount - lastVisibleItem

        if (scrollStateReset && remainingItemCount <= loadMoreThreshold) {
            scrollStateReset = false
            recyclerView.post {
                if (isEnabled) {
                    onLoadMore()
                }
            }
        }
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE || newState == RecyclerView.SCROLL_STATE_DRAGGING) {
            scrollStateReset = true
        }
    }
}
