package io.plastique.deviations.list

import androidx.recyclerview.widget.RecyclerView
import com.github.technoir42.glide.preloader.ListPreloader
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.core.image.ImageLoader
import io.plastique.core.image.TransformType
import io.plastique.core.lists.GridParams
import io.plastique.core.lists.ListItem

class DeviationsPreloaderFactory(
    private val imageLoader: ImageLoader,
    private val recyclerView: RecyclerView,
    private val adapter: ListDelegationAdapter<List<ListItem>>
) {
    fun createPreloader(layoutMode: LayoutMode, gridParams: GridParams): ListPreloader {
        return if (layoutMode == LayoutMode.Grid) {
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
            val request = imageLoader.load(preview.url)
                .params {
                    size = previewSize
                    transforms += TransformType.CenterCrop
                    cacheSource = true
                    cacheInMemory = false
                }
                .createPreloadRequest()
            preloader.preload(request, previewSize.width, previewSize.height)
        }

        return ListPreloader(imageLoader.glide, callback, MAX_PRELOAD_ITEMS_LIST)
    }

    private fun createGridPreloader(gridParams: GridParams): ListPreloader {
        val callback = ListPreloader.Callback { position, preloader ->
            val item = adapter.items[position] as? ImageDeviationItem ?: return@Callback

            val itemSize = gridParams.getItemSize(item.index)
            val image = ImageHelper.chooseThumbnail(item.thumbnails, itemSize.width)
            val request = imageLoader.load(image.url)
                .params {
                    cacheSource = true
                }
                .createPreloadRequest()
            preloader.preload(request, itemSize.width, itemSize.height)
        }

        return ListPreloader(imageLoader.glide, callback, MAX_PRELOAD_ROWS_GRID * gridParams.columnCount)
    }

    companion object {
        private const val MAX_PRELOAD_ITEMS_LIST = 4
        private const val MAX_PRELOAD_ROWS_GRID = 4
    }
}
