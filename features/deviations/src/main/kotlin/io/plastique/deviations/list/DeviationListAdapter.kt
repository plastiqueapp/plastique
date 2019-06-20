package io.plastique.deviations.list

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.technoir42.android.extensions.inflate
import com.google.android.flexbox.FlexboxLayoutManager
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.comments.CommentThreadId
import io.plastique.core.lists.BaseAdapterDelegate
import io.plastique.core.lists.ItemSizeCallback
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItemDelegate
import io.plastique.core.lists.OnViewHolderClickListener
import io.plastique.core.text.RichTextView
import io.plastique.deviations.Deviation
import io.plastique.deviations.DeviationActionsView
import io.plastique.deviations.R
import io.plastique.glide.GlideRequest
import io.plastique.glide.GlideRequests
import io.plastique.statuses.ShareObjectId
import io.plastique.util.dimensionRatio
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.Locale

private class ListImageDeviationItemDelegate(
    context: Context,
    private val glide: GlideRequests,
    private val layoutModeProvider: LayoutModeProvider,
    private val onViewHolderClickListener: OnViewHolderClickListener
) : BaseAdapterDelegate<ImageDeviationItem, ListItem, ListImageDeviationItemDelegate.ViewHolder>() {

    private val spacing = context.resources.getDimensionPixelOffset(R.dimen.deviations_list_spacing)

    override fun isForViewType(item: ListItem): Boolean =
        item is ImageDeviationItem && layoutModeProvider() == LayoutMode.List

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_deviation_image_list)
        return ViewHolder(view, onViewHolderClickListener, ImageHelper.getMaxWidth(parent))
    }

    override fun onBindViewHolder(item: ImageDeviationItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        (holder.itemView.layoutParams as ViewGroup.MarginLayoutParams).apply {
            topMargin = if (position > 0) spacing else 0
        }

        holder.titleView.text = item.title
        holder.actionsView.render(item.actionsState)

        val preview = ImageHelper.choosePreview(item.preview, item.content, holder.maxImageWidth)
        val previewSize = ImageHelper.calculateOptimalPreviewSize(preview, holder.maxImageWidth)
        (holder.imageView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = previewSize.dimensionRatio

        val thumbnailRequest = item.thumbnails.asSequence()
            .fold<Deviation.ImageInfo, GlideRequest<Drawable>?>(null) { previous, thumbnail ->
                val current = glide.load(thumbnail.url).onlyRetrieveFromCache(true)
                if (previous != null) {
                    current.thumbnail(previous)
                } else {
                    current
                }
            }

        glide.load(preview.url)
            .thumbnail(thumbnailRequest)
            .override(previewSize.width, previewSize.height)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.imageView)
    }

    class ViewHolder(
        itemView: View,
        private val onClickListener: OnViewHolderClickListener,
        val maxImageWidth: Int
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val titleView: TextView = itemView.findViewById(R.id.deviation_title)
        val imageView: ImageView = itemView.findViewById(R.id.deviation_image)
        val actionsView: DeviationActionsView = itemView.findViewById(R.id.deviation_actions)

        init {
            imageView.setOnClickListener(this)
            titleView.setOnClickListener(this)
            actionsView.setOnFavoriteClickListener(this)
            actionsView.setOnCommentsClickListener(this)
            actionsView.setOnShareClickListener(this)
        }

        override fun onClick(view: View) {
            onClickListener.onViewHolderClick(this, view)
        }
    }
}

class GridImageDeviationItemDelegate(
    context: Context,
    private val glide: GlideRequests,
    private val layoutModeProvider: LayoutModeProvider,
    private val itemSizeCallback: ItemSizeCallback,
    private val onViewHolderClickListener: OnViewHolderClickListener
) : BaseAdapterDelegate<ImageDeviationItem, ListItem, GridImageDeviationItemDelegate.ViewHolder>() {

    private val spacing = context.resources.getDimensionPixelOffset(R.dimen.deviations_grid_spacing)

    override fun isForViewType(item: ListItem): Boolean =
        item is ImageDeviationItem && layoutModeProvider() == LayoutMode.Grid

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view: View = parent.inflate(R.layout.item_deviation_image_grid)
        return ViewHolder(view, onViewHolderClickListener)
    }

    override fun onBindViewHolder(item: ImageDeviationItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        val itemSize = itemSizeCallback.getItemSize(item)
        val columnCount = itemSizeCallback.getColumnCount(item)

        (holder.itemView.layoutParams as FlexboxLayoutManager.LayoutParams).apply {
            width = itemSize.width
            height = itemSize.height
            leftMargin = if (item.index % columnCount != 0) spacing else 0
            topMargin = if (item.index >= columnCount) spacing else 0
        }

        holder.thumbnail.contentDescription = item.title

        val thumbnail = ImageHelper.chooseThumbnail(item.thumbnails, itemSize.width)
        glide.load(thumbnail.url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.thumbnail)
    }

    class ViewHolder(
        itemView: View,
        private val onClickListener: OnViewHolderClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val thumbnail: ImageView = itemView.findViewById(R.id.deviation_thumbnail)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            onClickListener.onViewHolderClick(this, view)
        }
    }
}

private class ListLiteratureDeviationItemDelegate(
    context: Context,
    private val layoutModeProvider: LayoutModeProvider,
    private val onViewHolderClickListener: OnViewHolderClickListener
) : BaseAdapterDelegate<LiteratureDeviationItem, ListItem, ListLiteratureDeviationItemDelegate.ViewHolder>() {

    private val spacing = context.resources.getDimensionPixelOffset(R.dimen.deviations_list_spacing)

    override fun isForViewType(item: ListItem): Boolean =
        item is LiteratureDeviationItem && layoutModeProvider() == LayoutMode.List

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_deviation_literature_list)
        return ViewHolder(view, onViewHolderClickListener)
    }

    override fun onBindViewHolder(item: LiteratureDeviationItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        (holder.itemView.layoutParams as ViewGroup.MarginLayoutParams).apply {
            topMargin = if (position > 0) spacing else 0
        }

        holder.titleView.text = item.title
        holder.excerptView.text = item.excerpt.value
        holder.actionsView.render(item.actionsState)
    }

    class ViewHolder(
        itemView: View,
        private val onClickListener: OnViewHolderClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val titleView: TextView = itemView.findViewById(R.id.deviation_title)
        val excerptView: RichTextView = itemView.findViewById(R.id.deviation_excerpt)
        val actionsView: DeviationActionsView = itemView.findViewById(R.id.deviation_actions)

        init {
            titleView.setOnClickListener(this)
            excerptView.setOnClickListener(this)
            actionsView.setOnFavoriteClickListener(this)
            actionsView.setOnCommentsClickListener(this)
            actionsView.setOnShareClickListener(this)
        }

        override fun onClick(view: View) {
            onClickListener.onViewHolderClick(this, view)
        }
    }
}

class GridLiteratureDeviationItemDelegate(
    context: Context,
    private val layoutModeProvider: LayoutModeProvider,
    private val itemSizeCallback: ItemSizeCallback,
    private val onViewHolderClickListener: OnViewHolderClickListener
) : BaseAdapterDelegate<LiteratureDeviationItem, ListItem, GridLiteratureDeviationItemDelegate.ViewHolder>() {

    private val spacing = context.resources.getDimensionPixelOffset(R.dimen.deviations_grid_spacing)

    override fun isForViewType(item: ListItem): Boolean =
        item is LiteratureDeviationItem && layoutModeProvider() == LayoutMode.Grid

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_deviation_literature_grid)
        return ViewHolder(view, onViewHolderClickListener)
    }

    override fun onBindViewHolder(item: LiteratureDeviationItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        val itemSize = itemSizeCallback.getItemSize(item)
        val columnCount = itemSizeCallback.getColumnCount(item)

        (holder.itemView.layoutParams as FlexboxLayoutManager.LayoutParams).apply {
            width = itemSize.width
            height = itemSize.height
            leftMargin = if (item.index % columnCount != 0) spacing else 0
            topMargin = if (item.index >= columnCount) spacing else 0
        }

        holder.title.text = item.title
    }

    class ViewHolder(
        itemView: View,
        private val onClickListener: OnViewHolderClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val title: TextView = itemView.findViewById(R.id.deviation_title)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            onClickListener.onViewHolderClick(this, view)
        }
    }
}

private class DateItemDelegate : BaseAdapterDelegate<DateItem, ListItem, DateItemDelegate.ViewHolder>() {
    override fun isForViewType(item: ListItem): Boolean = item is DateItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_deviations_date)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(item: DateItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.date.text = item.date.format(DATE_FORMATTER)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.date)
    }

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.ENGLISH)
    }
}

class DeviationsAdapter(
    context: Context,
    glide: GlideRequests,
    layoutModeProvider: LayoutModeProvider,
    itemSizeCallback: ItemSizeCallback,
    private val onDeviationClick: OnDeviationClickListener,
    private val onCommentsClick: OnCommentsClickListener,
    private val onFavoriteClick: OnFavoriteClickListener,
    private val onShareClick: OnShareClickListener
) : ListDelegationAdapter<List<ListItem>>(), OnViewHolderClickListener {

    init {
        delegatesManager.addDelegate(ListImageDeviationItemDelegate(context, glide, layoutModeProvider, this))
        delegatesManager.addDelegate(GridImageDeviationItemDelegate(context, glide, layoutModeProvider, itemSizeCallback, this))
        delegatesManager.addDelegate(ListLiteratureDeviationItemDelegate(context, layoutModeProvider, this))
        delegatesManager.addDelegate(GridLiteratureDeviationItemDelegate(context, layoutModeProvider, itemSizeCallback, this))
        delegatesManager.addDelegate(LoadingIndicatorItemDelegate())
        delegatesManager.addDelegate(DateItemDelegate())
    }

    override fun onViewHolderClick(holder: RecyclerView.ViewHolder, view: View) {
        val position = holder.adapterPosition
        if (position == RecyclerView.NO_POSITION) return
        val item = items[position] as DeviationItem
        when (view.id) {
            R.id.deviation_actions_comments -> {
                onCommentsClick(CommentThreadId.Deviation(item.deviationId))
            }

            R.id.deviation_actions_favorite -> {
                onFavoriteClick(item.deviationId, !item.actionsState.isFavorite)
            }

            R.id.deviation_actions_share -> {
                onShareClick(ShareObjectId.Deviation(item.deviationId))
            }

            else -> onDeviationClick(item.deviationId)
        }
    }
}

typealias LayoutModeProvider = () -> LayoutMode
typealias OnDeviationClickListener = (deviationId: String) -> Unit
typealias OnCommentsClickListener = (threadId: CommentThreadId) -> Unit
typealias OnFavoriteClickListener = (deviationId: String, favorite: Boolean) -> Unit
typealias OnShareClickListener = (shareObjectId: ShareObjectId) -> Unit
