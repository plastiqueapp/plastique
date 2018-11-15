package io.plastique.deviations.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.flexbox.FlexboxLayoutManager
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.core.lists.BaseAdapterDelegate
import io.plastique.core.lists.ItemSizeCallback
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItemDelegate
import io.plastique.core.lists.OnViewHolderClickListener
import io.plastique.deviations.Deviation
import io.plastique.deviations.R
import io.plastique.glide.GlideApp
import io.plastique.images.Image
import io.plastique.util.HtmlCompat
import io.plastique.util.Size
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.min

private const val MAX_IMAGE_WIDTH = 1080
private const val MAX_ASPECT_RATIO = 2.5 // height / width

typealias LayoutModeProvider = () -> LayoutMode

class ListImageDeviationItemDelegate(
    context: Context,
    private val layoutModeProvider: LayoutModeProvider,
    private val onViewHolderClickListener: OnViewHolderClickListener
) : BaseAdapterDelegate<DeviationItem, ListItem, ListImageDeviationItemDelegate.ViewHolder>() {

    private val spacing = context.resources.getDimensionPixelOffset(R.dimen.deviations_list_spacing)

    override fun isForViewType(item: ListItem): Boolean =
            item is DeviationItem && !item.deviation.isLiterature && layoutModeProvider() == LayoutMode.List

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_deviation_image_list, parent, false)
        return ViewHolder(view, onViewHolderClickListener, min(parent.width, MAX_IMAGE_WIDTH))
    }

    override fun onBindViewHolder(item: DeviationItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        (holder.itemView.layoutParams as ViewGroup.MarginLayoutParams).apply {
            topMargin = if (position > 0) spacing else 0
        }

        holder.title.text = item.deviation.title

        val image = chooseImage(item.deviation, holder.maxImageWidth)
        val size = calculateOptimalImageSize(image, holder.maxImageWidth)
        val layoutParams = holder.preview.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.dimensionRatio = "${size.width}:${size.height}"

        GlideApp.with(holder.itemView.context)
                .load(image.url)
                .override(size.width, size.height)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.preview)
    }

    private fun chooseImage(deviation: Deviation, maxImageWidth: Int): Image {
        val preview = deviation.preview
        return if (preview != null && preview.size.width >= maxImageWidth) preview else deviation.content!!
    }

    private fun calculateOptimalImageSize(image: Image, maxImageWidth: Int): Size {
        var imageWidth = image.size.width
        var imageHeight = image.size.height
        var aspectRatio = imageHeight / imageWidth.toDouble()
        if (aspectRatio > MAX_ASPECT_RATIO || imageWidth > maxImageWidth) {
            aspectRatio = min(aspectRatio, MAX_ASPECT_RATIO)
            imageWidth = min(imageWidth, maxImageWidth)
            imageHeight = (imageWidth * aspectRatio).toInt()
            return Size.of(imageWidth, imageHeight)
        }
        return image.size
    }

    class ViewHolder(
        itemView: View,
        private val onClickListener: OnViewHolderClickListener,
        val maxImageWidth: Int
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val title: TextView = itemView.findViewById(R.id.title)
        val preview: ImageView = itemView.findViewById(R.id.preview)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            onClickListener.onViewHolderClick(this, view)
        }
    }
}

class GridImageDeviationItemDelegate(
    context: Context,
    private val layoutModeProvider: LayoutModeProvider,
    private val itemSizeCallback: ItemSizeCallback,
    private val onViewHolderClickListener: OnViewHolderClickListener
) : BaseAdapterDelegate<DeviationItem, ListItem, GridImageDeviationItemDelegate.ViewHolder>() {

    private val spacing = context.resources.getDimensionPixelOffset(R.dimen.deviations_grid_spacing)

    override fun isForViewType(item: ListItem): Boolean =
            item is DeviationItem && !item.deviation.isLiterature && layoutModeProvider() == LayoutMode.Grid

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_deviation_image_grid, parent, false)
        return ViewHolder(view, onViewHolderClickListener)
    }

    override fun onBindViewHolder(item: DeviationItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        (holder.itemView.layoutParams as FlexboxLayoutManager.LayoutParams).apply {
            val itemSize = itemSizeCallback.getItemSize(item)
            val columnCount = itemSizeCallback.getColumnCount(item)
            width = itemSize.width
            height = itemSize.height
            leftMargin = if (item.index % columnCount != 0) spacing else 0
            topMargin = if (item.index >= columnCount) spacing else 0
        }

        val image = item.deviation.preview ?: item.deviation.content!!
        GlideApp.with(holder.thumbnail)
                .load(image.url)
                .centerCrop()
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

class ListLiteratureDeviationItemDelegate(
    context: Context,
    private val layoutModeProvider: LayoutModeProvider,
    private val onViewHolderClickListener: OnViewHolderClickListener
) : BaseAdapterDelegate<DeviationItem, ListItem, ListLiteratureDeviationItemDelegate.ViewHolder>() {

    private val spacing = context.resources.getDimensionPixelOffset(R.dimen.deviations_list_spacing)

    override fun isForViewType(item: ListItem): Boolean =
            item is DeviationItem && item.deviation.isLiterature && layoutModeProvider() == LayoutMode.List

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_deviation_literature_list, parent, false)
        return ViewHolder(view, onViewHolderClickListener)
    }

    override fun onBindViewHolder(item: DeviationItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        (holder.itemView.layoutParams as ViewGroup.MarginLayoutParams).apply {
            topMargin = if (position > 0) spacing else 0
        }

        holder.title.text = item.deviation.title
        holder.excerpt.text = HtmlCompat.fromHtml(item.deviation.excerpt!!)
    }

    class ViewHolder(
        itemView: View,
        private val onClickListener: OnViewHolderClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val title: TextView = itemView.findViewById(R.id.deviation_title)
        val excerpt: TextView = itemView.findViewById(R.id.deviation_excerpt)

        init {
            itemView.setOnClickListener(this)
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
) : BaseAdapterDelegate<DeviationItem, ListItem, GridLiteratureDeviationItemDelegate.ViewHolder>() {

    private val spacing = context.resources.getDimensionPixelOffset(R.dimen.deviations_grid_spacing)

    override fun isForViewType(item: ListItem): Boolean =
            item is DeviationItem && item.deviation.isLiterature && layoutModeProvider() == LayoutMode.Grid

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_deviation_literature_grid, parent, false)
        return ViewHolder(view, onViewHolderClickListener)
    }

    override fun onBindViewHolder(item: DeviationItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        (holder.itemView.layoutParams as FlexboxLayoutManager.LayoutParams).apply {
            val itemSize = itemSizeCallback.getItemSize(item)
            val columnCount = itemSizeCallback.getColumnCount(item)
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

class DateItemDelegate : BaseAdapterDelegate<DateItem, ListItem, DateItemDelegate.ViewHolder>() {
    private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)

    override fun isForViewType(item: ListItem): Boolean = item is DateItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(item: DateItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.date.text = item.date.format(dateFormatter)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.date)
    }
}

class DeviationsAdapter(context: Context, layoutModeProvider: LayoutModeProvider, itemSizeCallback: ItemSizeCallback) : ListDelegationAdapter<List<ListItem>>(), OnViewHolderClickListener {
    var onDeviationClickListener: OnDeviationClickListener? = null

    init {
        delegatesManager.addDelegate(ListImageDeviationItemDelegate(context, layoutModeProvider, this))
        delegatesManager.addDelegate(GridImageDeviationItemDelegate(context, layoutModeProvider, itemSizeCallback, this))
        delegatesManager.addDelegate(ListLiteratureDeviationItemDelegate(context, layoutModeProvider, this))
        delegatesManager.addDelegate(GridLiteratureDeviationItemDelegate(context, layoutModeProvider, itemSizeCallback, this))
        delegatesManager.addDelegate(LoadingIndicatorItemDelegate())
        delegatesManager.addDelegate(DateItemDelegate())
    }

    override fun onViewHolderClick(holder: RecyclerView.ViewHolder, view: View) {
        val position = holder.adapterPosition
        val item = if (position != RecyclerView.NO_POSITION) items[position] else return
        when (item) {
            is DeviationItem -> onDeviationClickListener?.invoke(item.deviation)
        }
    }
}

typealias OnDeviationClickListener = (Deviation) -> Unit
