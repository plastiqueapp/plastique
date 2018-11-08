package io.plastique.core.lists

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager

class EndlessScrollListener(
    var loadThreshold: Int,
    var isEnabled: Boolean = true,
    private var loadMoreListener: LoadMoreListener
) : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (!isEnabled) {
            return
        }
        val layoutManager = recyclerView.layoutManager ?: return
        val totalItemCount = layoutManager.itemCount
        val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
        val remainingItemCount = totalItemCount - lastVisibleItem

        if (remainingItemCount <= loadThreshold) {
            recyclerView.post {
                if (isEnabled) {
                    loadMoreListener()
                }
            }
        }
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {}

    private fun RecyclerView.LayoutManager.findLastVisibleItemPosition(): Int = when (this) {
        is FlexboxLayoutManager -> findLastVisibleItemPosition()
        is LinearLayoutManager -> findLastVisibleItemPosition()
        else -> throw UnsupportedOperationException("$javaClass is not supported by EndlessScrollListener")
    }
}

typealias LoadMoreListener = () -> Unit
