package io.plastique.collections

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.github.technoir42.android.extensions.inflate
import com.google.android.flexbox.FlexboxLayoutManager
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.collections.folders.Folder
import io.plastique.core.image.ImageLoader
import io.plastique.core.image.TransformType
import io.plastique.core.lists.BaseAdapterDelegate
import io.plastique.core.lists.ItemSizeCallback
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItemDelegate
import io.plastique.deviations.OnDeviationClickListener
import io.plastique.deviations.list.GridImageDeviationItemDelegate
import io.plastique.deviations.list.GridLiteratureDeviationItemDelegate
import io.plastique.deviations.list.GridVideoDeviationItemDelegate
import io.plastique.deviations.list.LayoutMode

private class FolderItemDelegate(
    private val imageLoader: ImageLoader,
    private val itemSizeCallback: ItemSizeCallback,
    private val onFolderClick: OnCollectionFolderClickListener,
    private val onFolderLongClick: OnCollectionFolderLongClickListener
) : BaseAdapterDelegate<FolderItem, ListItem, FolderItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is FolderItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_collections_folder)
        return ViewHolder(view, onFolderClick, onFolderLongClick)
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
        imageLoader.load(item.folder.thumbnailUrl)
            .params {
                transforms += TransformType.CenterCrop
            }
            .into(holder.thumbnail)
    }

    class ViewHolder(
        itemView: View,
        onFolderClick: OnCollectionFolderClickListener,
        onFolderLongClick: OnCollectionFolderLongClickListener
    ) : BaseAdapterDelegate.ViewHolder<FolderItem>(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.folder_thumbnail)
        val name: TextView = itemView.findViewById(R.id.folder_name)
        val size: TextView = itemView.findViewById(R.id.folder_size)

        init {
            itemView.setOnClickListener { onFolderClick(item.folder.id, item.folder.name) }
            itemView.setOnLongClickListener { view -> onFolderLongClick(item.folder, view) }
        }
    }
}

private class HeaderItemDelegate : BaseAdapterDelegate<HeaderItem, ListItem, HeaderItemDelegate.ViewHolder>() {
    override fun isForViewType(item: ListItem): Boolean = item is HeaderItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_collections_header)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(item: HeaderItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.title.text = item.title
    }

    class ViewHolder(itemView: View) : BaseAdapterDelegate.ViewHolder<HeaderItem>(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
    }
}

internal class CollectionsAdapter(
    imageLoader: ImageLoader,
    itemSizeCallback: ItemSizeCallback,
    onFolderClick: OnCollectionFolderClickListener,
    onFolderLongClick: OnCollectionFolderLongClickListener,
    onDeviationClick: OnDeviationClickListener
) : ListDelegationAdapter<List<ListItem>>() {

    init {
        val layoutModeProvider = { LayoutMode.Grid }
        delegatesManager.addDelegate(FolderItemDelegate(imageLoader, itemSizeCallback, onFolderClick, onFolderLongClick))
        delegatesManager.addDelegate(HeaderItemDelegate())
        delegatesManager.addDelegate(GridImageDeviationItemDelegate(imageLoader, layoutModeProvider, itemSizeCallback, onDeviationClick))
        delegatesManager.addDelegate(GridLiteratureDeviationItemDelegate(layoutModeProvider, itemSizeCallback, onDeviationClick))
        delegatesManager.addDelegate(GridVideoDeviationItemDelegate(imageLoader, layoutModeProvider, itemSizeCallback, onDeviationClick))
        delegatesManager.addDelegate(LoadingIndicatorItemDelegate())
    }
}

private typealias OnCollectionFolderLongClickListener = (Folder, View) -> Boolean
