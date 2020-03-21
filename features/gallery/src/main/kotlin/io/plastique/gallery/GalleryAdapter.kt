package io.plastique.gallery

import android.view.View
import android.view.ViewGroup
import com.github.technoir42.android.extensions.layoutInflater
import com.google.android.flexbox.FlexboxLayoutManager
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
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
import io.plastique.gallery.databinding.ItemGalleryFolderBinding
import io.plastique.gallery.databinding.ItemGalleryHeaderBinding
import io.plastique.gallery.folders.Folder
import io.plastique.gallery.folders.GalleryFolderId

private class FolderItemDelegate(
    private val imageLoader: ImageLoader,
    private val itemSizeCallback: ItemSizeCallback,
    private val onFolderClick: OnGalleryFolderClickListener,
    private val onFolderLongClick: OnGalleryFolderLongClickListener
) : BaseAdapterDelegate<FolderItem, ListItem, FolderItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is FolderItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val binding = ItemGalleryFolderBinding.inflate(parent.layoutInflater, parent, false)
        return ViewHolder(binding, onFolderClick, onFolderLongClick)
    }

    override fun onBindViewHolder(item: FolderItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        val resources = holder.binding.root.resources
        (holder.binding.root.layoutParams as FlexboxLayoutManager.LayoutParams).apply {
            val itemSize = itemSizeCallback.getItemSize(item)
            val spacing = resources.getDimensionPixelOffset(R.dimen.gallery_folder_spacing)
            width = itemSize.width
            height = itemSize.height
            leftMargin = if (item.index % itemSizeCallback.getColumnCount(item) != 0) spacing else 0
            topMargin = if (item.index >= itemSizeCallback.getColumnCount(item)) spacing else 0
        }

        holder.binding.folderName.text = item.folder.name
        holder.binding.folderSize.text = item.folder.size.toString()

        // TODO: Placeholder for null thumbnailUrl
        imageLoader.load(item.folder.thumbnailUrl)
            .params {
                transforms += TransformType.CenterCrop
            }
            .into(holder.binding.thumbnail)
    }

    class ViewHolder(
        val binding: ItemGalleryFolderBinding,
        onFolderClick: OnGalleryFolderClickListener,
        onFolderLongClick: OnGalleryFolderLongClickListener
    ) : BaseAdapterDelegate.ViewHolder<FolderItem>(binding.root) {

        init {
            binding.root.setOnClickListener { onFolderClick(item.folder.id, item.folder.name) }
            binding.root.setOnLongClickListener { view -> onFolderLongClick(item.folder, view) }
        }
    }
}

private class HeaderItemDelegate : BaseAdapterDelegate<HeaderItem, ListItem, HeaderItemDelegate.ViewHolder>() {
    override fun isForViewType(item: ListItem): Boolean = item is HeaderItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val binding = ItemGalleryHeaderBinding.inflate(parent.layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(item: HeaderItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.binding.title.text = item.title
    }

    class ViewHolder(val binding: ItemGalleryHeaderBinding) : BaseAdapterDelegate.ViewHolder<HeaderItem>(binding.root)
}

internal class GalleryAdapter(
    imageLoader: ImageLoader,
    itemSizeCallback: ItemSizeCallback,
    onFolderClick: OnGalleryFolderClickListener,
    onFolderLongClick: OnGalleryFolderLongClickListener,
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

private typealias OnGalleryFolderClickListener = (folderId: GalleryFolderId, folderName: String) -> Unit
private typealias OnGalleryFolderLongClickListener = (Folder, View) -> Boolean
