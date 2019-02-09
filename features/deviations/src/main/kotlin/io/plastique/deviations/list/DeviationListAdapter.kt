package io.plastique.deviations.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.flexbox.FlexboxLayoutManager
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.comments.CommentThreadId
import io.plastique.core.lists.BaseAdapterDelegate
import io.plastique.core.lists.ItemSizeCallback
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItemDelegate
import io.plastique.core.lists.OnViewHolderClickListener
import io.plastique.deviations.R
import io.plastique.glide.GlideRequests
import io.plastique.statuses.ShareObjectId
import io.plastique.util.dimensionRatio
import org.threeten.bp.format.DateTimeFormatter
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_deviation_image_list, parent, false)
        return ViewHolder(view, onViewHolderClickListener, ImageHelper.getMaxWidth(parent))
    }

    override fun onBindViewHolder(item: ImageDeviationItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        (holder.itemView.layoutParams as ViewGroup.MarginLayoutParams).apply {
            topMargin = if (position > 0) spacing else 0
        }

        holder.titleView.text = item.deviation.title
        holder.commentsButton.text = item.deviation.stats.comments.toString()
        holder.commentsButton.isVisible = item.deviation.properties.allowsComments
        holder.favoriteButton.text = item.deviation.stats.favorites.toString()
        holder.favoriteButton.isChecked = item.deviation.properties.isFavorite

        val preview = ImageHelper.choosePreview(item.deviation, holder.maxImageWidth)
        val previewSize = ImageHelper.calculateOptimalPreviewSize(preview, holder.maxImageWidth)
        val layoutParams = holder.imageView.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.dimensionRatio = previewSize.dimensionRatio

        glide.load(preview.url)
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
        val commentsButton: TextView = itemView.findViewById(R.id.button_comments)
        val favoriteButton: CheckedTextView = itemView.findViewById(R.id.button_favorite)
        private val shareButton: View = itemView.findViewById(R.id.button_share)

        init {
            imageView.setOnClickListener(this)
            titleView.setOnClickListener(this)
            commentsButton.setOnClickListener(this)
            favoriteButton.setOnClickListener(this)
            shareButton.setOnClickListener(this)
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
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_deviation_image_grid, parent, false)
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

        val thumbnail = ImageHelper.chooseThumbnail(item.deviation, itemSize.width)
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_deviation_literature_list, parent, false)
        return ViewHolder(view, onViewHolderClickListener)
    }

    override fun onBindViewHolder(item: LiteratureDeviationItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        (holder.itemView.layoutParams as ViewGroup.MarginLayoutParams).apply {
            topMargin = if (position > 0) spacing else 0
        }

        holder.titleView.text = item.deviation.title
        holder.excerptView.text = item.excerpt.value
        holder.commentsButton.text = item.deviation.stats.comments.toString()
        holder.commentsButton.isVisible = item.deviation.properties.allowsComments
        holder.favoriteButton.text = item.deviation.stats.favorites.toString()
        holder.favoriteButton.isChecked = item.deviation.properties.isFavorite
    }

    class ViewHolder(
        itemView: View,
        private val onClickListener: OnViewHolderClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val titleView: TextView = itemView.findViewById(R.id.deviation_title)
        val excerptView: TextView = itemView.findViewById(R.id.deviation_excerpt)
        val commentsButton: TextView = itemView.findViewById(R.id.button_comments)
        val favoriteButton: CheckedTextView = itemView.findViewById(R.id.button_favorite)
        private val shareButton: View = itemView.findViewById(R.id.button_share)

        init {
            titleView.setOnClickListener(this)
            excerptView.setOnClickListener(this)
            commentsButton.setOnClickListener(this)
            favoriteButton.setOnClickListener(this)
            shareButton.setOnClickListener(this)
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_deviation_literature_grid, parent, false)
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

        holder.title.text = item.deviation.title
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
    private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)

    override fun isForViewType(item: ListItem): Boolean = item is DateItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_deviations_date, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(item: DateItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.date.text = item.date.format(dateFormatter)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.date)
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
        val item = if (position != RecyclerView.NO_POSITION) items[position] as DeviationItem else return

        when (view.id) {
            R.id.button_comments -> {
                onCommentsClick(CommentThreadId.Deviation(item.deviation.id))
            }

            R.id.button_favorite -> {
                onFavoriteClick(item.deviation.id, !item.deviation.properties.isFavorite)
            }

            R.id.button_share -> {
                onShareClick(ShareObjectId.Deviation(item.deviation.id))
            }

            else -> onDeviationClick(item.deviation.id)
        }
    }
}

typealias LayoutModeProvider = () -> LayoutMode
typealias OnDeviationClickListener = (deviationId: String) -> Unit
typealias OnCommentsClickListener = (threadId: CommentThreadId) -> Unit
typealias OnFavoriteClickListener = (deviationId: String, favorite: Boolean) -> Unit
typealias OnShareClickListener = (shareObjectId: ShareObjectId) -> Unit
