package io.plastique.deviations.list

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.technoir42.glide.preloader.ListPreloader
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.core.lists.GridParams
import io.plastique.core.lists.ListItem
import io.plastique.glide.GlideRequests

class DeviationsPreloaderFactory(
    private val glide: GlideRequests,
    private val recyclerView: RecyclerView,
    private val adapter: ListDelegationAdapter<List<ListItem>>
) {
    fun createPreloader(layoutMode: LayoutMode, gridParams: GridParams): ListPreloader {
        return if (layoutMode === LayoutMode.Grid) {
            createGridPreloader(gridParams)
        } else {
            createListPreloader()
        }
    }

    private fun createListPreloader(): ListPreloader {
        val callback = ListPreloader.Callback { position, preloader ->
            val item = adapter.items[position] as? ImageDeviationItem ?: return@Callback

            val maxImageWidth = ImageHelper.getMaxWidth(recyclerView)
            val preview = ImageHelper.choosePreview(item.preview, item.content, maxImageWidth)
            val previewSize = ImageHelper.calculateOptimalPreviewSize(preview, maxImageWidth)
            val request = glide.load(preview.url)
                .override(previewSize.width, previewSize.height)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.LOW)
                .skipMemoryCache(true)

            preloader.preload(request, previewSize.width, previewSize.height)
        }

        return ListPreloader(glide, callback, MAX_PRELOAD_ITEMS_LIST)
    }

    private fun createGridPreloader(gridParams: GridParams): ListPreloader {
        val callback = ListPreloader.Callback { position, preloader ->
            val item = adapter.items[position] as? ImageDeviationItem ?: return@Callback

            val itemSize = gridParams.getItemSize(item.index)
            val image = ImageHelper.chooseThumbnail(item.thumbnails, itemSize.width)
            val request = glide.load(image.url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.LOW)

            preloader.preload(request, itemSize.width, itemSize.height)
        }

        return ListPreloader(glide, callback, MAX_PRELOAD_ROWS_GRID * gridParams.columnCount)
    }

    companion object {
        private const val MAX_PRELOAD_ITEMS_LIST = 4
        private const val MAX_PRELOAD_ROWS_GRID = 4
    }
}
