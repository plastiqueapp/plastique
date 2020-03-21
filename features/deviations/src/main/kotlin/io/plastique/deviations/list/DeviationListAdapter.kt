package io.plastique.deviations.list

import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.technoir42.android.extensions.layoutInflater
import com.google.android.flexbox.FlexboxLayoutManager
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.comments.OnCommentsClickListener
import io.plastique.core.image.ImageLoader
import io.plastique.core.image.TransformType
import io.plastique.core.lists.BaseAdapterDelegate
import io.plastique.core.lists.ItemSizeCallback
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItemDelegate
import io.plastique.core.time.print
import io.plastique.deviations.OnDeviationClickListener
import io.plastique.deviations.OnFavoriteClickListener
import io.plastique.deviations.R
import io.plastique.deviations.databinding.ItemDeviationImageGridBinding
import io.plastique.deviations.databinding.ItemDeviationImageListBinding
import io.plastique.deviations.databinding.ItemDeviationLiteratureGridBinding
import io.plastique.deviations.databinding.ItemDeviationLiteratureListBinding
import io.plastique.deviations.databinding.ItemDeviationVideoGridBinding
import io.plastique.deviations.databinding.ItemDeviationVideoListBinding
import io.plastique.deviations.databinding.ItemDeviationsDateBinding
import io.plastique.statuses.OnShareClickListener
import io.plastique.util.dimensionRatio
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.Locale

// region Grid
class GridImageDeviationItemDelegate(
    private val imageLoader: ImageLoader,
    private val layoutModeProvider: LayoutModeProvider,
    private val itemSizeCallback: ItemSizeCallback,
    private val onDeviationClick: OnDeviationClickListener
) : BaseAdapterDelegate<ImageDeviationItem, ListItem, GridImageDeviationItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean =
        item is ImageDeviationItem && layoutModeProvider() == LayoutMode.Grid

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val binding = ItemDeviationImageGridBinding.inflate(parent.layoutInflater, parent, false)
        return ViewHolder(binding, onDeviationClick)
    }

    override fun onBindViewHolder(item: ImageDeviationItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        val itemSize = itemSizeCallback.getItemSize(item)
        val columnCount = itemSizeCallback.getColumnCount(item)

        (holder.itemView.layoutParams as FlexboxLayoutManager.LayoutParams).apply {
            width = itemSize.width
            height = itemSize.height

            val spacing = holder.itemView.resources.getDimensionPixelOffset(R.dimen.deviations_grid_spacing)
            leftMargin = if (item.index % columnCount != 0) spacing else 0
            topMargin = if (item.index >= columnCount) spacing else 0
        }

        holder.binding.thumbnail.contentDescription = item.title

        val thumbnail = ImageHelper.chooseThumbnail(item.thumbnails, itemSize.width)
        imageLoader.load(thumbnail.url)
            .params {
                placeholderDrawable = R.drawable.deviations_placeholder_background
                cacheSource = true
            }
            .into(holder.binding.thumbnail)
    }

    class ViewHolder(
        val binding: ItemDeviationImageGridBinding,
        onDeviationClick: OnDeviationClickListener
    ) : BaseAdapterDelegate.ViewHolder<ImageDeviationItem>(binding.root) {

        init {
            binding.root.setOnClickListener { onDeviationClick(item.deviationId) }
        }
    }
}

class GridLiteratureDeviationItemDelegate(
    private val layoutModeProvider: LayoutModeProvider,
    private val itemSizeCallback: ItemSizeCallback,
    private val onDeviationClick: OnDeviationClickListener
) : BaseAdapterDelegate<LiteratureDeviationItem, ListItem, GridLiteratureDeviationItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean =
        item is LiteratureDeviationItem && layoutModeProvider() == LayoutMode.Grid

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val binding = ItemDeviationLiteratureGridBinding.inflate(parent.layoutInflater, parent, false)
        return ViewHolder(binding, onDeviationClick)
    }

    override fun onBindViewHolder(item: LiteratureDeviationItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        val itemSize = itemSizeCallback.getItemSize(item)
        val columnCount = itemSizeCallback.getColumnCount(item)

        (holder.itemView.layoutParams as FlexboxLayoutManager.LayoutParams).apply {
            width = itemSize.width
            height = itemSize.height

            val spacing = holder.itemView.resources.getDimensionPixelOffset(R.dimen.deviations_grid_spacing)
            leftMargin = if (item.index % columnCount != 0) spacing else 0
            topMargin = if (item.index >= columnCount) spacing else 0
        }

        holder.binding.title.text = item.title
    }

    class ViewHolder(
        val binding: ItemDeviationLiteratureGridBinding,
        onDeviationClick: OnDeviationClickListener
    ) : BaseAdapterDelegate.ViewHolder<LiteratureDeviationItem>(binding.root) {

        init {
            binding.root.setOnClickListener { onDeviationClick(item.deviationId) }
        }
    }
}

class GridVideoDeviationItemDelegate(
    private val imageLoader: ImageLoader,
    private val layoutModeProvider: LayoutModeProvider,
    private val itemSizeCallback: ItemSizeCallback,
    private val onDeviationClick: OnDeviationClickListener
) : BaseAdapterDelegate<VideoDeviationItem, ListItem, GridVideoDeviationItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean =
        item is VideoDeviationItem && layoutModeProvider() == LayoutMode.Grid

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val binding = ItemDeviationVideoGridBinding.inflate(parent.layoutInflater, parent, false)
        return ViewHolder(binding, onDeviationClick)
    }

    override fun onBindViewHolder(item: VideoDeviationItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        val itemSize = itemSizeCallback.getItemSize(item)
        val columnCount = itemSizeCallback.getColumnCount(item)

        (holder.itemView.layoutParams as FlexboxLayoutManager.LayoutParams).apply {
            width = itemSize.width
            height = itemSize.height

            val spacing = holder.itemView.resources.getDimensionPixelOffset(R.dimen.deviations_grid_spacing)
            leftMargin = if (item.index % columnCount != 0) spacing else 0
            topMargin = if (item.index >= columnCount) spacing else 0
        }

        holder.binding.thumbnail.contentDescription = item.title
        holder.binding.duration.value.text = item.duration.print()

        val thumbnail = ImageHelper.chooseThumbnail(item.thumbnails, itemSize.width)
        imageLoader.load(thumbnail.url)
            .params {
                placeholderDrawable = R.drawable.deviations_placeholder_background
                cacheSource = true
            }
            .into(holder.binding.thumbnail)
    }

    class ViewHolder(
        val binding: ItemDeviationVideoGridBinding,
        onDeviationClick: OnDeviationClickListener
    ) : BaseAdapterDelegate.ViewHolder<VideoDeviationItem>(binding.root) {

        init {
            binding.root.setOnClickListener { onDeviationClick(item.deviationId) }
        }
    }
}
// endregion

// region List
private class ListImageDeviationItemDelegate(
    private val imageLoader: ImageLoader,
    private val layoutModeProvider: LayoutModeProvider,
    private val onCommentsClick: OnCommentsClickListener,
    private val onDeviationClick: OnDeviationClickListener,
    private val onFavoriteClick: OnFavoriteClickListener,
    private val onShareClick: OnShareClickListener
) : BaseAdapterDelegate<ImageDeviationItem, ListItem, ListImageDeviationItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean =
        item is ImageDeviationItem && layoutModeProvider() == LayoutMode.List

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val binding = ItemDeviationImageListBinding.inflate(parent.layoutInflater, parent, false)
        return ViewHolder(binding, onCommentsClick, onDeviationClick, onFavoriteClick, onShareClick, ImageHelper.getMaxWidth(parent))
    }

    override fun onBindViewHolder(item: ImageDeviationItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        (holder.itemView.layoutParams as ViewGroup.MarginLayoutParams).apply {
            val spacing = holder.itemView.resources.getDimensionPixelOffset(R.dimen.deviations_list_spacing)
            topMargin = if (position > 0) spacing else 0
        }

        holder.binding.title.text = item.title
        holder.binding.actions.render(item.actionsState)

        val preview = ImageHelper.choosePreview(item.preview, item.content, holder.maxImageWidth)
        val previewSize = ImageHelper.calculateOptimalPreviewSize(preview, holder.maxImageWidth)
        (holder.binding.preview.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = previewSize.dimensionRatio

        imageLoader.load(preview.url)
            .params {
                size = previewSize
                placeholderDrawable = R.drawable.deviations_placeholder_background
                thumbnailUrls = item.thumbnails.map { it.url }
                transforms += TransformType.CenterCrop
                cacheSource = true
            }
            .into(holder.binding.preview)
    }

    class ViewHolder(
        val binding: ItemDeviationImageListBinding,
        onCommentsClick: OnCommentsClickListener,
        onDeviationClick: OnDeviationClickListener,
        onFavoriteClick: OnFavoriteClickListener,
        onShareClick: OnShareClickListener,
        val maxImageWidth: Int
    ) : BaseAdapterDelegate.ViewHolder<ImageDeviationItem>(binding.root) {

        init {
            val onDeviationClickListener = View.OnClickListener { onDeviationClick(item.deviationId) }
            binding.preview.setOnClickListener(onDeviationClickListener)
            binding.title.setOnClickListener(onDeviationClickListener)
            binding.actions.onCommentsClick = onCommentsClick
            binding.actions.onFavoriteClick = onFavoriteClick
            binding.actions.onShareClick = onShareClick
        }
    }
}

private class ListLiteratureDeviationItemDelegate(
    private val layoutModeProvider: LayoutModeProvider,
    private val onCommentsClick: OnCommentsClickListener,
    private val onDeviationClick: OnDeviationClickListener,
    private val onFavoriteClick: OnFavoriteClickListener,
    private val onShareClick: OnShareClickListener
) : BaseAdapterDelegate<LiteratureDeviationItem, ListItem, ListLiteratureDeviationItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean =
        item is LiteratureDeviationItem && layoutModeProvider() == LayoutMode.List

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val binding = ItemDeviationLiteratureListBinding.inflate(parent.layoutInflater, parent, false)
        return ViewHolder(binding, onCommentsClick, onDeviationClick, onFavoriteClick, onShareClick)
    }

    override fun onBindViewHolder(item: LiteratureDeviationItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        (holder.itemView.layoutParams as ViewGroup.MarginLayoutParams).apply {
            val spacing = holder.itemView.resources.getDimensionPixelOffset(R.dimen.deviations_list_spacing)
            topMargin = if (position > 0) spacing else 0
        }

        holder.binding.title.text = item.title
        holder.binding.excerpt.text = item.excerpt.value
        holder.binding.actions.render(item.actionsState)
    }

    class ViewHolder(
        val binding: ItemDeviationLiteratureListBinding,
        onCommentsClick: OnCommentsClickListener,
        onDeviationClick: OnDeviationClickListener,
        onFavoriteClick: OnFavoriteClickListener,
        onShareClick: OnShareClickListener
    ) : BaseAdapterDelegate.ViewHolder<LiteratureDeviationItem>(binding.root) {

        init {
            val onDeviationClickListener = View.OnClickListener { onDeviationClick(item.deviationId) }
            binding.title.setOnClickListener(onDeviationClickListener)
            binding.excerpt.setOnClickListener(onDeviationClickListener)
            binding.actions.onCommentsClick = onCommentsClick
            binding.actions.onFavoriteClick = onFavoriteClick
            binding.actions.onShareClick = onShareClick
        }
    }
}

private class ListVideoDeviationItemDelegate(
    private val imageLoader: ImageLoader,
    private val layoutModeProvider: LayoutModeProvider,
    private val onCommentsClick: OnCommentsClickListener,
    private val onDeviationClick: OnDeviationClickListener,
    private val onFavoriteClick: OnFavoriteClickListener,
    private val onShareClick: OnShareClickListener
) : BaseAdapterDelegate<VideoDeviationItem, ListItem, ListVideoDeviationItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean =
        item is VideoDeviationItem && layoutModeProvider() == LayoutMode.List

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val binding = ItemDeviationVideoListBinding.inflate(parent.layoutInflater, parent, false)
        return ViewHolder(binding, onCommentsClick, onDeviationClick, onFavoriteClick, onShareClick)
    }

    override fun onBindViewHolder(item: VideoDeviationItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        (holder.itemView.layoutParams as ViewGroup.MarginLayoutParams).apply {
            val spacing = holder.itemView.resources.getDimensionPixelOffset(R.dimen.deviations_list_spacing)
            topMargin = if (position > 0) spacing else 0
        }

        holder.binding.title.text = item.title
        holder.binding.actions.render(item.actionsState)
        holder.binding.duration.value.text = item.duration.print()

        val preview = item.preview
        (holder.binding.preview.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = preview.size.dimensionRatio

        imageLoader.load(preview.url)
            .params {
                size = preview.size
                placeholderDrawable = R.drawable.deviations_placeholder_background
                cacheSource = true
            }
            .into(holder.binding.preview)
    }

    class ViewHolder(
        val binding: ItemDeviationVideoListBinding,
        onCommentsClick: OnCommentsClickListener,
        onDeviationClick: OnDeviationClickListener,
        onFavoriteClick: OnFavoriteClickListener,
        onShareClick: OnShareClickListener
    ) : BaseAdapterDelegate.ViewHolder<VideoDeviationItem>(binding.root) {

        init {
            val onDeviationClickListener = View.OnClickListener { onDeviationClick(item.deviationId) }
            binding.preview.setOnClickListener(onDeviationClickListener)
            binding.title.setOnClickListener(onDeviationClickListener)
            binding.actions.onCommentsClick = onCommentsClick
            binding.actions.onFavoriteClick = onFavoriteClick
            binding.actions.onShareClick = onShareClick
        }
    }
}
// endregion

private class DateItemDelegate : BaseAdapterDelegate<DateItem, ListItem, DateItemDelegate.ViewHolder>() {
    override fun isForViewType(item: ListItem): Boolean = item is DateItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val binding = ItemDeviationsDateBinding.inflate(parent.layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(item: DateItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.binding.date.text = item.date.format(DATE_FORMATTER)
    }

    class ViewHolder(val binding: ItemDeviationsDateBinding) : BaseAdapterDelegate.ViewHolder<DateItem>(binding.root)

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.ENGLISH)
    }
}

internal class DeviationListAdapter(
    imageLoader: ImageLoader,
    layoutModeProvider: LayoutModeProvider,
    itemSizeCallback: ItemSizeCallback,
    onDeviationClick: OnDeviationClickListener,
    onCommentsClick: OnCommentsClickListener,
    onFavoriteClick: OnFavoriteClickListener,
    onShareClick: OnShareClickListener
) : ListDelegationAdapter<List<ListItem>>() {

    init {
        delegatesManager.addDelegate(GridImageDeviationItemDelegate(imageLoader, layoutModeProvider, itemSizeCallback, onDeviationClick))
        delegatesManager.addDelegate(GridLiteratureDeviationItemDelegate(layoutModeProvider, itemSizeCallback, onDeviationClick))
        delegatesManager.addDelegate(GridVideoDeviationItemDelegate(imageLoader, layoutModeProvider, itemSizeCallback, onDeviationClick))

        delegatesManager.addDelegate(
            ListImageDeviationItemDelegate(imageLoader, layoutModeProvider, onCommentsClick, onDeviationClick, onFavoriteClick, onShareClick))
        delegatesManager.addDelegate(
            ListLiteratureDeviationItemDelegate(layoutModeProvider, onCommentsClick, onDeviationClick, onFavoriteClick, onShareClick))
        delegatesManager.addDelegate(
            ListVideoDeviationItemDelegate(imageLoader, layoutModeProvider, onCommentsClick, onDeviationClick, onFavoriteClick, onShareClick))

        delegatesManager.addDelegate(LoadingIndicatorItemDelegate())
        delegatesManager.addDelegate(DateItemDelegate())
    }
}

private typealias LayoutModeProvider = () -> LayoutMode
