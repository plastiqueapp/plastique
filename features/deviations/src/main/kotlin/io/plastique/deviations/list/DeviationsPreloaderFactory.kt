package io.plastique.deviations.list

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.core.lists.GridParams
import io.plastique.core.lists.ListItem
import io.plastique.glide.GlideApp
import io.plastique.glide.GlideRequests
import io.plastique.glide.RecyclerViewPreloader

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
        val preloadModelProvider = object : ListPreloader.PreloadModelProvider<DeviationItem> {
            override fun getPreloadItems(position: Int): List<DeviationItem> {
                return when (val item = adapter.items[position]) {
                    is ImageDeviationItem -> listOf(item)
                    else -> emptyList()
                }
            }

            override fun getPreloadRequestBuilder(item: DeviationItem): RequestBuilder<*>? {
                val maxImageWidth = ImageHelper.getMaxWidth(recyclerView)
                val preview = ImageHelper.choosePreview(item.deviation, maxImageWidth)
                val previewSize = ImageHelper.calculateOptimalPreviewSize(preview, maxImageWidth)
                return glide.load(preview.url)
                    .override(previewSize.width, previewSize.height)
                    .centerCrop()
                    .priority(Priority.LOW)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
            }
        }

        val preloadSizeProvider = ListPreloader.PreloadSizeProvider<DeviationItem> { item, _, _ ->
            val maxImageWidth = ImageHelper.getMaxWidth(recyclerView)
            val image = ImageHelper.choosePreview(item.deviation, maxImageWidth)
            val size = ImageHelper.calculateOptimalPreviewSize(image, maxImageWidth)
            intArrayOf(size.width, size.height)
        }

        return RecyclerViewPreloader(glide, lifecycle, preloadModelProvider, preloadSizeProvider, MAX_PRELOAD_ITEMS_LIST)
    }

    private fun createGridPreloader(gridParams: GridParams): RecyclerViewPreloader<*> {
        val preloadModelProvider = object : ListPreloader.PreloadModelProvider<DeviationItem> {
            override fun getPreloadItems(position: Int): List<DeviationItem> {
                return when (val item = adapter.items[position]) {
                    is ImageDeviationItem -> listOf(item)
                    else -> emptyList()
                }
            }

            override fun getPreloadRequestBuilder(item: DeviationItem): RequestBuilder<*>? {
                val itemSize = gridParams.getItemSize(item.index)
                val image = ImageHelper.chooseThumbnail(item.deviation, itemSize.width)
                return glide.load(image.url)
                    .priority(Priority.LOW)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
            }
        }

        val preloadSizeProvider = ListPreloader.PreloadSizeProvider<DeviationItem> { item, _, _ ->
            val itemSize = gridParams.getItemSize(item.index)
            intArrayOf(itemSize.width, itemSize.height)
        }

        return RecyclerViewPreloader(glide, lifecycle, preloadModelProvider, preloadSizeProvider, MAX_PRELOAD_ROWS_GRID * gridParams.columnCount)
    }

    companion object {
        private const val MAX_PRELOAD_ITEMS_LIST = 4
        private const val MAX_PRELOAD_ROWS_GRID = 4
    }
}
