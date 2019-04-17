package io.plastique.collections

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
import io.plastique.core.lists.OnViewHolderLongClickListener
import io.plastique.deviations.list.DeviationItem
import io.plastique.deviations.list.GridImageDeviationItemDelegate
import io.plastique.deviations.list.GridLiteratureDeviationItemDelegate
import io.plastique.deviations.list.LayoutMode
import io.plastique.glide.GlideRequests

private class FolderItemDelegate(
    private val glide: GlideRequests,
    private val itemSizeCallback: ItemSizeCallback,
    private val onViewHolderClickListener: OnViewHolderClickListener,
    private val onViewHolderLongClickListener: OnViewHolderLongClickListener
) : BaseAdapterDelegate<FolderItem, ListItem, FolderItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is FolderItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_collections_folder, parent, false)
        return ViewHolder(view, onViewHolderClickListener, onViewHolderLongClickListener)
    }

    override fun onBindViewHolder(item: FolderItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        (holder.itemView.layoutParams as FlexboxLayoutManager.LayoutParams).apply {
            val itemSize = itemSizeCallback.getItemSize(item)
            val spacing = holder.itemView.resources.getDimensionPixelOffset(R.dimen.collections_folder_spacing)
            width = itemSize.width
            height = itemSize.height
            leftMargin = if (item.index % itemSizeCallback.getColumnCount(item) != 0) spacing else 0
            topMargin = if (item.index >= itemSizeCallback.getColumnCount(item)) spacing else 0
        }

        holder.name.text = item.folder.name
        holder.size.text = item.folder.size.toString()

        // TODO: Placeholder for null thumbnailUrl
        glide.load(item.folder.thumbnailUrl)
                .centerCrop()
                .into(holder.thumbnail)
    }

    class ViewHolder(
        itemView: View,
        private val onClickListener: OnViewHolderClickListener,
        private val onLongClickListener: OnViewHolderLongClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        val thumbnail: ImageView = itemView.findViewById(R.id.folder_thumbnail)
        val name: TextView = itemView.findViewById(R.id.folder_name)
        val size: TextView = itemView.findViewById(R.id.folder_size)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(view: View) {
            onClickListener.onViewHolderClick(this, view)
        }

        override fun onLongClick(view: View): Boolean {
            return onLongClickListener.onViewHolderLongClick(this, view)
        }
    }
}

private class HeaderItemDelegate : BaseAdapterDelegate<HeaderItem, ListItem, HeaderItemDelegate.ViewHolder>() {
    override fun isForViewType(item: ListItem): Boolean = item is HeaderItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_collections_header, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(item: HeaderItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.title.text = item.title
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
    }
}

class CollectionsAdapter(
    context: Context,
    glide: GlideRequests,
    itemSizeCallback: ItemSizeCallback,
    private val onFolderClick: OnFolderClickListener,
    private val onFolderLongClick: OnFolderLongClickListener,
    private val onDeviationClick: OnDeviationClickListener
) : ListDelegationAdapter<List<ListItem>>(), OnViewHolderClickListener, OnViewHolderLongClickListener {

    init {
        val layoutModeProvider = { LayoutMode.Grid }
        delegatesManager.addDelegate(FolderItemDelegate(glide, itemSizeCallback, this, this))
        delegatesManager.addDelegate(HeaderItemDelegate())
        delegatesManager.addDelegate(GridImageDeviationItemDelegate(context, glide, layoutModeProvider, itemSizeCallback, this))
        delegatesManager.addDelegate(GridLiteratureDeviationItemDelegate(context, layoutModeProvider, itemSizeCallback, this))
        delegatesManager.addDelegate(LoadingIndicatorItemDelegate())
    }

    override fun onViewHolderClick(holder: RecyclerView.ViewHolder, view: View) {
        val position = holder.adapterPosition
        if (position == RecyclerView.NO_POSITION) return
        when (val item = items[position]) {
            is FolderItem -> onFolderClick(item)
            is DeviationItem -> onDeviationClick(item.deviation.id)
        }
    }

    override fun onViewHolderLongClick(holder: RecyclerView.ViewHolder, view: View): Boolean {
        val position = holder.adapterPosition
        if (position == RecyclerView.NO_POSITION) return false
        return when (val item = items[position]) {
            is FolderItem -> onFolderLongClick(item, view)
            else -> throw IllegalStateException("Unhandled item type ${item.javaClass}")
        }
    }
}

typealias OnFolderClickListener = (FolderItem) -> Unit
typealias OnFolderLongClickListener = (FolderItem, View) -> Boolean
typealias OnDeviationClickListener = (deviationId: String) -> Unit
