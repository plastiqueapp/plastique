package io.plastique.gallery

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.core.lists.BaseAdapterDelegate
import io.plastique.core.lists.ItemSizeCallback
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItemDelegate
import io.plastique.core.lists.OnViewHolderClickListener
import io.plastique.deviations.list.DeviationItem
import io.plastique.deviations.list.GridImageDeviationItemDelegate
import io.plastique.deviations.list.GridLiteratureDeviationItemDelegate
import io.plastique.deviations.list.LayoutMode
import io.plastique.glide.GlideApp

class FolderItemDelegate(
    private val itemSizeCallback: ItemSizeCallback,
    private val listener: OnViewHolderClickListener
) : BaseAdapterDelegate<FolderItem, ListItem, FolderItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is FolderItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_folder, parent, false)
        return ViewHolder(view, listener)
    }

    override fun onBindViewHolder(item: FolderItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        (holder.itemView.layoutParams as FlexboxLayoutManager.LayoutParams).apply {
            val itemSize = itemSizeCallback.getItemSize(item)
            val spacing = holder.itemView.resources.getDimensionPixelOffset(R.dimen.gallery_folder_spacing)
            width = itemSize.width
            height = itemSize.height
            leftMargin = if (item.index % itemSizeCallback.getColumnCount(item) != 0) spacing else 0
            topMargin = if (item.index >= itemSizeCallback.getColumnCount(item)) spacing else 0
        }

        holder.name.text = item.folder.name
        holder.size.text = item.folder.size.toString()

        // TODO: Placeholder for null thumbnailUrl
        GlideApp.with(holder.thumbnail)
                .load(item.folder.thumbnailUrl)
                .centerCrop()
                .into(holder.thumbnail)
    }

    class ViewHolder(
        itemView: View,
        private val listener: OnViewHolderClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val thumbnail: ImageView = itemView.findViewById(R.id.folder_thumbnail)
        val name: TextView = itemView.findViewById(R.id.folder_name)
        val size: TextView = itemView.findViewById(R.id.folder_size)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            listener.onViewHolderClick(this, view)
        }
    }
}

class HeaderItemDelegate : BaseAdapterDelegate<HeaderItem, ListItem, HeaderItemDelegate.ViewHolder>() {
    override fun isForViewType(item: ListItem): Boolean = item is HeaderItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_header, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(item: HeaderItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.title.text = item.title
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
    }
}

class GalleryAdapter(context: Context, itemSizeCallback: ItemSizeCallback) : ListDelegationAdapter<List<ListItem>>(), OnViewHolderClickListener {
    var onFolderClickListener: OnFolderClickListener? = null
    var onDeviationClickListener: OnDeviationClickListener? = null

    init {
        val layoutModeProvider = { LayoutMode.Grid }
        delegatesManager.addDelegate(FolderItemDelegate(itemSizeCallback, this))
        delegatesManager.addDelegate(HeaderItemDelegate())
        delegatesManager.addDelegate(GridImageDeviationItemDelegate(context, layoutModeProvider, itemSizeCallback, this))
        delegatesManager.addDelegate(GridLiteratureDeviationItemDelegate(context, layoutModeProvider, itemSizeCallback, this))
        delegatesManager.addDelegate(LoadingIndicatorItemDelegate())
    }

    override fun onViewHolderClick(holder: RecyclerView.ViewHolder, view: View) {
        val position = holder.adapterPosition
        val item = if (position != RecyclerView.NO_POSITION) items[position] else return
        when (item) {
            is FolderItem -> onFolderClickListener?.invoke(item)
            is DeviationItem -> onDeviationClickListener?.invoke(item)
        }
    }
}

typealias OnFolderClickListener = (FolderItem) -> Unit
typealias OnDeviationClickListener = (DeviationItem) -> Unit