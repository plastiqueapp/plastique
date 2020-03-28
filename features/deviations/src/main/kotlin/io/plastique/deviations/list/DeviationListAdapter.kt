package io.plastique.deviations.list

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.technoir42.android.extensions.inflate
import com.google.android.flexbox.FlexboxLayoutManager
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.comments.OnCommentsClickListener
import io.plastique.core.image.ImageLoader
import io.plastique.core.image.TransformType
import io.plastique.core.lists.BaseAdapterDelegate
import io.plastique.core.lists.ItemSizeCallback
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItemDelegate
import io.plastique.core.text.RichTextView
import io.plastique.core.time.print
import io.plastique.deviations.DeviationActionsView
import io.plastique.deviations.OnDeviationClickListener
import io.plastique.deviations.OnFavoriteClickListener
import io.plastique.deviations.R
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
        val view: View = parent.inflate(R.layout.item_deviation_image_grid)
        return ViewHolder(view, onDeviationClick)
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

        holder.thumbnail.contentDescription = item.title

        val thumbnail = ImageHelper.chooseThumbnail(item.thumbnails, itemSize.width)
        imageLoader.load(thumbnail.url)
            .params {
                placeholderDrawable = R.drawable.deviations_placeholder_background
                cacheSource = true
            }
            .into(holder.thumbnail)
    }

    class ViewHolder(
        itemView: View,
        onDeviationClick: OnDeviationClickListener
    ) : BaseAdapterDelegate.ViewHolder<ImageDeviationItem>(itemView) {

        val thumbnail: ImageView = itemView.findViewById(R.id.deviation_thumbnail)

        init {
            itemView.setOnClickListener { onDeviationClick(item.deviationId) }
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
        val view = parent.inflate(R.layout.item_deviation_literature_grid)
        return ViewHolder(view, onDeviationClick)
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

        holder.title.text = item.title
    }

    class ViewHolder(
        itemView: View,
        onDeviationClick: OnDeviationClickListener
    ) : BaseAdapterDelegate.ViewHolder<LiteratureDeviationItem>(itemView) {

        val title: TextView = itemView.findViewById(R.id.deviation_title)

        init {
            itemView.setOnClickListener { onDeviationClick(item.deviationId) }
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
        val view = parent.inflate(R.layout.item_deviation_video_grid)
        return ViewHolder(view, onDeviationClick)
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

        holder.thumbnail.contentDescription = item.title
        holder.durationView.text = item.duration.print()

        val thumbnail = ImageHelper.chooseThumbnail(item.thumbnails, itemSize.width)
        imageLoader.load(thumbnail.url)
            .params {
                placeholderDrawable = R.drawable.deviations_placeholder_background
                cacheSource = true
            }
            .into(holder.thumbnail)
    }

    class ViewHolder(
        itemView: View,
        onDeviationClick: OnDeviationClickListener
    ) : BaseAdapterDelegate.ViewHolder<VideoDeviationItem>(itemView) {

        val thumbnail: ImageView = itemView.findViewById(R.id.deviation_thumbnail)
        val durationView: TextView = itemView.findViewById(R.id.deviation_video_duration)

        init {
            itemView.setOnClickListener { onDeviationClick(item.deviationId) }
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
        val view = parent.inflate(R.layout.item_deviation_image_list)
        return ViewHolder(view, onCommentsClick, onDeviationClick, onFavoriteClick, onShareClick, ImageHelper.getMaxWidth(parent))
    }

    override fun onBindViewHolder(item: ImageDeviationItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        (holder.itemView.layoutParams as ViewGroup.MarginLayoutParams).apply {
            val spacing = holder.itemView.resources.getDimensionPixelOffset(R.dimen.deviations_list_spacing)
            topMargin = if (position > 0) spacing else 0
        }

        holder.titleView.text = item.title
        holder.actionsView.render(item.actionsState)

        val preview = ImageHelper.choosePreview(item.preview, item.content, holder.maxImageWidth)
        val previewSize = ImageHelper.calculateOptimalPreviewSize(preview, holder.maxImageWidth)
        (holder.previewView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = previewSize.dimensionRatio

        imageLoader.load(preview.url)
            .params {
                size = previewSize
                placeholderDrawable = R.drawable.deviations_placeholder_background
                thumbnailUrls = item.thumbnails.map { it.url }
                transforms += TransformType.CenterCrop
                cacheSource = true
            }
            .into(holder.previewView)
    }

    class ViewHolder(
        itemView: View,
        onCommentsClick: OnCommentsClickListener,
        onDeviationClick: OnDeviationClickListener,
        onFavoriteClick: OnFavoriteClickListener,
        onShareClick: OnShareClickListener,
        val maxImageWidth: Int
    ) : BaseAdapterDelegate.ViewHolder<ImageDeviationItem>(itemView) {

        val titleView: TextView = itemView.findViewById(R.id.deviation_title)
        val previewView: ImageView = itemView.findViewById(R.id.deviation_preview)
        val actionsView: DeviationActionsView = itemView.findViewById(R.id.deviation_actions)

        init {
            val onDeviationClickListener = View.OnClickListener { onDeviationClick(item.deviationId) }
            previewView.setOnClickListener(onDeviationClickListener)
            titleView.setOnClickListener(onDeviationClickListener)
            actionsView.onCommentsClick = onCommentsClick
            actionsView.onFavoriteClick = onFavoriteClick
            actionsView.onShareClick = onShareClick
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
        val view = parent.inflate(R.layout.item_deviation_literature_list)
        return ViewHolder(view, onCommentsClick, onDeviationClick, onFavoriteClick, onShareClick)
    }

    override fun onBindViewHolder(item: LiteratureDeviationItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        (holder.itemView.layoutParams as ViewGroup.MarginLayoutParams).apply {
            val spacing = holder.itemView.resources.getDimensionPixelOffset(R.dimen.deviations_list_spacing)
            topMargin = if (position > 0) spacing else 0
        }

        holder.titleView.text = item.title
        holder.excerptView.text = item.excerpt.value
        holder.actionsView.render(item.actionsState)
    }

    class ViewHolder(
        itemView: View,
        onCommentsClick: OnCommentsClickListener,
        onDeviationClick: OnDeviationClickListener,
        onFavoriteClick: OnFavoriteClickListener,
        onShareClick: OnShareClickListener
    ) : BaseAdapterDelegate.ViewHolder<LiteratureDeviationItem>(itemView) {

        val titleView: TextView = itemView.findViewById(R.id.deviation_title)
        val excerptView: RichTextView = itemView.findViewById(R.id.deviation_excerpt)
        val actionsView: DeviationActionsView = itemView.findViewById(R.id.deviation_actions)

        init {
            val onDeviationClickListener = View.OnClickListener { onDeviationClick(item.deviationId) }
            titleView.setOnClickListener(onDeviationClickListener)
            excerptView.setOnClickListener(onDeviationClickListener)
            actionsView.onCommentsClick = onCommentsClick
            actionsView.onFavoriteClick = onFavoriteClick
            actionsView.onShareClick = onShareClick
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
        val view = parent.inflate(R.layout.item_deviation_video_list)
        return ViewHolder(view, onCommentsClick, onDeviationClick, onFavoriteClick, onShareClick)
    }

    override fun onBindViewHolder(item: VideoDeviationItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        (holder.itemView.layoutParams as ViewGroup.MarginLayoutParams).apply {
            val spacing = holder.itemView.resources.getDimensionPixelOffset(R.dimen.deviations_list_spacing)
            topMargin = if (position > 0) spacing else 0
        }

        holder.titleView.text = item.title
        holder.actionsView.render(item.actionsState)
        holder.durationView.text = item.duration.print()

        val preview = item.preview
        (holder.imageView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = preview.size.dimensionRatio

        imageLoader.load(preview.url)
            .params {
                size = preview.size
                placeholderDrawable = R.drawable.deviations_placeholder_background
                cacheSource = true
            }
            .into(holder.imageView)
    }

    class ViewHolder(
        itemView: View,
        onCommentsClick: OnCommentsClickListener,
        onDeviationClick: OnDeviationClickListener,
        onFavoriteClick: OnFavoriteClickListener,
        onShareClick: OnShareClickListener
    ) : BaseAdapterDelegate.ViewHolder<VideoDeviationItem>(itemView) {

        val titleView: TextView = itemView.findViewById(R.id.deviation_title)
        val imageView: ImageView = itemView.findViewById(R.id.deviation_preview)
        val durationView: TextView = itemView.findViewById(R.id.deviation_video_duration)
        val actionsView: DeviationActionsView = itemView.findViewById(R.id.deviation_actions)

        init {
            val onDeviationClickListener = View.OnClickListener { onDeviationClick(item.deviationId) }
            imageView.setOnClickListener(onDeviationClickListener)
            titleView.setOnClickListener(onDeviationClickListener)
            actionsView.onCommentsClick = onCommentsClick
            actionsView.onFavoriteClick = onFavoriteClick
            actionsView.onShareClick = onShareClick
        }
    }
}
// endregion

private class DateItemDelegate : BaseAdapterDelegate<DateItem, ListItem, DateItemDelegate.ViewHolder>() {
    override fun isForViewType(item: ListItem): Boolean = item is DateItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_deviations_date)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(item: DateItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.date.text = item.date.format(DATE_FORMATTER)
    }

    class ViewHolder(itemView: View) : BaseAdapterDelegate.ViewHolder<DateItem>(itemView) {
        val date: TextView = itemView.findViewById(R.id.date)
    }

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
