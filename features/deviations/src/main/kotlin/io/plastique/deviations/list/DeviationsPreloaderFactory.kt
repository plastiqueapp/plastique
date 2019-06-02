package io.plastique.deviations.list

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.core.lists.GridParams
import io.plastique.core.lists.ListItem
import io.plastique.glide.GlideApp
import io.plastique.glide.GlideRequests
import io.plastique.glide.RecyclerViewPreloader
import io.plastique.util.Size

class DeviationsPreloaderFactory private constructor(
    private val glide: GlideRequests,
    private val lifecycle: Lifecycle,
    private val recyclerView: RecyclerView,
    private val adapter: ListDelegationAdapter<List<ListItem>>
) {
    constructor(fragment: Fragment, recyclerView: RecyclerView, adapter: ListDelegationAdapter<List<ListItem>>) :
            this(GlideApp.with(fragment), fragment.lifecycle, recyclerView, adapter)

    fun createPreloader(layoutMode: LayoutMode, gridParams: GridParams): RecyclerViewPreloader<*> {
        return if (layoutMode === LayoutMode.Grid) {
            createGridPreloader(gridParams)
        } else {
            createListPreloader()
        }
    }

    private fun createListPreloader(): RecyclerViewPreloader<*> {
        val callback = object : RecyclerViewPreloader.Callback<DeviationItem> {
            override fun getPreloadItems(position: Int): List<DeviationItem> {
                return when (val item = adapter.items[position]) {
                    is ImageDeviationItem -> listOf(item)
                    else -> emptyList()
                }
            }

            override fun createRequestBuilder(item: DeviationItem): RequestBuilder<*> {
                val maxImageWidth = ImageHelper.getMaxWidth(recyclerView)
                val preview = ImageHelper.choosePreview(item.deviation, maxImageWidth)
                val previewSize = ImageHelper.calculateOptimalPreviewSize(preview, maxImageWidth)
                return glide.load(preview.url)
                    .override(previewSize.width, previewSize.height)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.LOW)
            }

            override fun getPreloadSize(item: DeviationItem): Size {
                val maxImageWidth = ImageHelper.getMaxWidth(recyclerView)
                val image = ImageHelper.choosePreview(item.deviation, maxImageWidth)
                return ImageHelper.calculateOptimalPreviewSize(image, maxImageWidth)
            }
        }

        return RecyclerViewPreloader(glide, lifecycle, callback, MAX_PRELOAD_ITEMS_LIST)
    }

    private fun createGridPreloader(gridParams: GridParams): RecyclerViewPreloader<*> {
        val callback = object : RecyclerViewPreloader.Callback<DeviationItem> {
            override fun getPreloadItems(position: Int): List<DeviationItem> {
                return when (val item = adapter.items[position]) {
                    is ImageDeviationItem -> listOf(item)
                    else -> emptyList()
                }
            }

            override fun createRequestBuilder(item: DeviationItem): RequestBuilder<*> {
                val itemSize = gridParams.getItemSize(item.index)
                val image = ImageHelper.chooseThumbnail(item.deviation, itemSize.width)
                return glide.load(image.url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.LOW)
            }

            override fun getPreloadSize(item: DeviationItem): Size {
                return gridParams.getItemSize(item.index)
            }
        }

        return RecyclerViewPreloader(glide, lifecycle, callback, maxPreload = MAX_PRELOAD_ROWS_GRID * gridParams.columnCount)
    }

    companion object {
        private const val MAX_PRELOAD_ITEMS_LIST = 4
        private const val MAX_PRELOAD_ROWS_GRID = 4
    }
}
