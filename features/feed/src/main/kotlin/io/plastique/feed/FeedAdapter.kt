package io.plastique.feed

import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.core.text.htmlEncode
import androidx.recyclerview.widget.RecyclerView
import com.github.technoir42.android.extensions.disableChangeAnimations
import com.github.technoir42.android.extensions.inflate
import com.google.android.flexbox.FlexboxLayoutManager
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.collections.OnCollectionFolderClickListener
import io.plastique.comments.OnCommentsClickListener
import io.plastique.common.FeedHeaderView
import io.plastique.core.image.ImageLoader
import io.plastique.core.image.TransformType
import io.plastique.core.lists.BaseAdapterDelegate
import io.plastique.core.lists.ItemSizeCallback
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItemDelegate
import io.plastique.core.text.RichTextView
import io.plastique.core.time.ElapsedTimeFormatter
import io.plastique.deviations.DeviationActionsView
import io.plastique.deviations.OnDeviationClickListener
import io.plastique.deviations.OnFavoriteClickListener
import io.plastique.deviations.list.GridImageDeviationItemDelegate
import io.plastique.deviations.list.GridLiteratureDeviationItemDelegate
import io.plastique.deviations.list.GridVideoDeviationItemDelegate
import io.plastique.deviations.list.ImageHelper
import io.plastique.deviations.list.LayoutMode
import io.plastique.statuses.OnShareClickListener
import io.plastique.statuses.OnStatusClickListener
import io.plastique.statuses.ShareUiModel
import io.plastique.statuses.ShareView
import io.plastique.statuses.StatusActionsView
import io.plastique.statuses.isDeleted
import io.plastique.users.OnUserClickListener
import io.plastique.util.dimensionRatio

private class CollectionUpdateItemDelegate(
    private val imageLoader: ImageLoader,
    private val elapsedTimeFormatter: ElapsedTimeFormatter,
    private val itemSizeCallback: ItemSizeCallback,
    private val onCollectionFolderClick: OnCollectionFolderClickListener,
    private val onDeviationClick: OnDeviationClickListener,
    private val onUserClick: OnUserClickListener
) : BaseAdapterDelegate<CollectionUpdateItem, ListItem, CollectionUpdateItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is CollectionUpdateItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_feed_collection_update)
        return ViewHolder(view, imageLoader, itemSizeCallback, onCollectionFolderClick, onDeviationClick, onUserClick)
    }

    override fun onBindViewHolder(item: CollectionUpdateItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.headerView.time = elapsedTimeFormatter.format(item.date)
        holder.headerView.setUser(item.user, imageLoader)
        holder.folderNameView.text = holder.itemView.resources.getString(R.string.feed_collection_folder_name, item.folderName, item.addedCount)
        holder.folderItemsAdapter.update(item.folderItems)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        (holder as ViewHolder).run { folderItemsView.layoutManager = layoutManager }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        (holder as ViewHolder).run { folderItemsView.layoutManager = null }
    }

    class ViewHolder(
        itemView: View,
        imageLoader: ImageLoader,
        itemSizeCallback: ItemSizeCallback,
        onCollectionFolderClick: OnCollectionFolderClickListener,
        onDeviationClick: OnDeviationClickListener,
        onUserClick: OnUserClickListener
    ) : BaseAdapterDelegate.ViewHolder<CollectionUpdateItem>(itemView) {
        val headerView: FeedHeaderView = itemView.findViewById(R.id.header)
        val folderNameView: TextView = itemView.findViewById(R.id.folder_name)
        val folderItemsView: RecyclerView = itemView.findViewById(R.id.thumbnails)
        val folderItemsAdapter: DeviationsAdapter = DeviationsAdapter(imageLoader, itemSizeCallback, onDeviationClick)
        val layoutManager: FlexboxLayoutManager = FlexboxLayoutManager(folderItemsView.context)

        init {
            headerView.onUserClick = onUserClick
            folderNameView.setOnClickListener { onCollectionFolderClick(item.folderId, item.folderName) }
            folderItemsView.adapter = folderItemsAdapter
            folderItemsView.disableChangeAnimations()

            // TODO: Thumbnail click listener
        }
    }
}

private class ImageDeviationItemDelegate(
    private val imageLoader: ImageLoader,
    private val elapsedTimeFormatter: ElapsedTimeFormatter,
    private val onDeviationClick: OnDeviationClickListener,
    private val onCommentsClick: OnCommentsClickListener,
    private val onFavoriteClick: OnFavoriteClickListener,
    private val onShareClick: OnShareClickListener,
    private val onUserClick: OnUserClickListener
) : BaseAdapterDelegate<ImageDeviationItem, ListItem, ImageDeviationItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is ImageDeviationItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_feed_deviation_image)
        return ViewHolder(view, onDeviationClick, onCommentsClick, onFavoriteClick, onShareClick, onUserClick, ImageHelper.getMaxWidth(parent))
    }

    override fun onBindViewHolder(item: ImageDeviationItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.headerView.time = elapsedTimeFormatter.format(item.date)
        holder.headerView.setUser(item.user, imageLoader)
        holder.titleView.text = item.title
        holder.actionsView.render(item.actionsState)

        val preview = ImageHelper.choosePreview(item.preview, item.content, holder.maxImageWidth)
        val previewSize = ImageHelper.calculateOptimalPreviewSize(preview, holder.maxImageWidth)
        (holder.previewView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = previewSize.dimensionRatio

        imageLoader.load(preview.url)
            .params {
                size = previewSize
                transforms += TransformType.CenterCrop
                cacheSource = true
            }
            .into(holder.previewView)
    }

    class ViewHolder(
        itemView: View,
        onDeviationClick: OnDeviationClickListener,
        onCommentsClick: OnCommentsClickListener,
        onFavoriteClick: OnFavoriteClickListener,
        onShareClick: OnShareClickListener,
        onUserClick: OnUserClickListener,
        val maxImageWidth: Int
    ) : BaseAdapterDelegate.ViewHolder<ImageDeviationItem>(itemView) {
        val headerView: FeedHeaderView = itemView.findViewById(R.id.header)
        val previewView: ImageView = itemView.findViewById(R.id.deviation_preview)
        val titleView: TextView = itemView.findViewById(R.id.deviation_title)
        val actionsView: DeviationActionsView = itemView.findViewById(R.id.deviation_actions)

        init {
            val onDeviationClickListener = View.OnClickListener { onDeviationClick(item.deviationId) }
            previewView.setOnClickListener(onDeviationClickListener)
            titleView.setOnClickListener(onDeviationClickListener)
            headerView.onUserClick = onUserClick
            actionsView.onCommentsClick = onCommentsClick
            actionsView.onFavoriteClick = onFavoriteClick
            actionsView.onShareClick = onShareClick
        }
    }
}

private class LiteratureDeviationItemDelegate(
    private val imageLoader: ImageLoader,
    private val elapsedTimeFormatter: ElapsedTimeFormatter,
    private val onDeviationClick: OnDeviationClickListener,
    private val onCommentsClick: OnCommentsClickListener,
    private val onFavoriteClick: OnFavoriteClickListener,
    private val onShareClick: OnShareClickListener,
    private val onUserClick: OnUserClickListener
) : BaseAdapterDelegate<LiteratureDeviationItem, ListItem, LiteratureDeviationItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is LiteratureDeviationItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_feed_deviation_literature)
        return ViewHolder(view, onDeviationClick, onCommentsClick, onFavoriteClick, onShareClick, onUserClick)
    }

    override fun onBindViewHolder(item: LiteratureDeviationItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.headerView.time = elapsedTimeFormatter.format(item.date)
        holder.headerView.setUser(item.user, imageLoader)
        holder.titleView.text = item.title
        holder.excerptView.text = item.excerpt.value
        holder.actionsView.render(item.actionsState)
    }

    class ViewHolder(
        itemView: View,
        onDeviationClick: OnDeviationClickListener,
        onCommentsClick: OnCommentsClickListener,
        onFavoriteClick: OnFavoriteClickListener,
        onShareClick: OnShareClickListener,
        onUserClick: OnUserClickListener
    ) : BaseAdapterDelegate.ViewHolder<LiteratureDeviationItem>(itemView) {
        val headerView: FeedHeaderView = itemView.findViewById(R.id.header)
        val titleView: TextView = itemView.findViewById(R.id.deviation_title)
        val excerptView: RichTextView = itemView.findViewById(R.id.deviation_excerpt)
        val actionsView: DeviationActionsView = itemView.findViewById(R.id.deviation_actions)

        init {
            val onDeviationClickListener = View.OnClickListener { onDeviationClick(item.deviationId) }
            titleView.setOnClickListener(onDeviationClickListener)
            excerptView.setOnClickListener(onDeviationClickListener)
            headerView.onUserClick = onUserClick
            actionsView.onCommentsClick = onCommentsClick
            actionsView.onFavoriteClick = onFavoriteClick
            actionsView.onShareClick = onShareClick
        }
    }
}

private class MultipleDeviationsItemDelegate(
    private val imageLoader: ImageLoader,
    private val elapsedTimeFormatter: ElapsedTimeFormatter,
    private val gridItemSizeCallback: ItemSizeCallback,
    private val onDeviationClick: OnDeviationClickListener,
    private val onUserClick: OnUserClickListener
) : BaseAdapterDelegate<MultipleDeviationsItem, ListItem, MultipleDeviationsItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is MultipleDeviationsItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_feed_multiple_deviations)
        return ViewHolder(view, imageLoader, gridItemSizeCallback, onDeviationClick, onUserClick)
    }

    override fun onBindViewHolder(item: MultipleDeviationsItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        val resources = holder.itemView.resources
        holder.headerView.time = elapsedTimeFormatter.format(item.date)
        holder.headerView.setUser(item.user, imageLoader)
        holder.descriptionView.text = resources.getString(R.string.feed_multiple_deviations_submitted_description,
            resources.getQuantityString(R.plurals.common_deviations, item.submittedTotal, item.submittedTotal))
        holder.adapter.update(item.items)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        (holder as ViewHolder).run { deviationsView.layoutManager = holder.layoutManager }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        (holder as ViewHolder).run { deviationsView.layoutManager = null }
    }

    class ViewHolder(
        itemView: View,
        imageLoader: ImageLoader,
        gridItemSizeCallback: ItemSizeCallback,
        onDeviationClick: OnDeviationClickListener,
        onUserClick: OnUserClickListener
    ) : BaseAdapterDelegate.ViewHolder<MultipleDeviationsItem>(itemView) {
        val headerView: FeedHeaderView = itemView.findViewById(R.id.header)
        val descriptionView: TextView = itemView.findViewById(R.id.description)
        val deviationsView: RecyclerView = itemView.findViewById(R.id.deviations)
        val adapter: DeviationsAdapter = DeviationsAdapter(imageLoader, gridItemSizeCallback, onDeviationClick)
        val layoutManager: FlexboxLayoutManager = FlexboxLayoutManager(deviationsView.context)

        init {
            headerView.onUserClick = onUserClick
            deviationsView.adapter = adapter
            deviationsView.disableChangeAnimations()
        }
    }
}

private class StatusItemDelegate(
    private val imageLoader: ImageLoader,
    private val elapsedTimeFormatter: ElapsedTimeFormatter,
    private val onStatusClick: OnStatusClickListener,
    private val onDeviationClick: OnDeviationClickListener,
    private val onCommentsClick: OnCommentsClickListener,
    private val onShareClick: OnShareClickListener,
    private val onUserClick: OnUserClickListener
) : BaseAdapterDelegate<StatusUpdateItem, ListItem, StatusItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is StatusUpdateItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_feed_status)
        return ViewHolder(view, onStatusClick, onDeviationClick, onCommentsClick, onShareClick, onUserClick)
    }

    override fun onBindViewHolder(item: StatusUpdateItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.headerView.time = elapsedTimeFormatter.format(item.date)
        holder.headerView.setUser(item.user, imageLoader)
        holder.statusTextView.text = item.text.value
        holder.actionsView.render(item.actionsState)

        holder.shareView.setShare(item.share, imageLoader, elapsedTimeFormatter)
        holder.shareView.isClickable = !item.share.isDeleted
    }

    class ViewHolder(
        itemView: View,
        onStatusClick: OnStatusClickListener,
        onDeviationClick: OnDeviationClickListener,
        onCommentsClick: OnCommentsClickListener,
        onShareClick: OnShareClickListener,
        onUserClick: OnUserClickListener
    ) : BaseAdapterDelegate.ViewHolder<StatusUpdateItem>(itemView) {
        val headerView: FeedHeaderView = itemView.findViewById(R.id.header)
        val statusTextView: RichTextView = itemView.findViewById(R.id.status_text)
        val shareView: ShareView = itemView.findViewById(R.id.status_share)
        val actionsView: StatusActionsView = itemView.findViewById(R.id.status_actions)

        init {
            statusTextView.setOnClickListener { onStatusClick(item.statusId) }
            statusTextView.movementMethod = LinkMovementMethod.getInstance()
            shareView.setOnClickListener {
                when (val share = item.share) {
                    is ShareUiModel.ImageDeviation -> onDeviationClick(share.deviationId)
                    is ShareUiModel.LiteratureDeviation -> onDeviationClick(share.deviationId)
                    is ShareUiModel.Status -> onStatusClick(share.statusId)
                }
            }
            headerView.onUserClick = onUserClick
            actionsView.onCommentsClick = onCommentsClick
            actionsView.onShareClick = onShareClick
        }
    }
}

private class UsernameChangeItemDelegate(
    private val imageLoader: ImageLoader,
    private val elapsedTimeFormatter: ElapsedTimeFormatter,
    private val onUserClick: OnUserClickListener
) : BaseAdapterDelegate<UsernameChangeItem, ListItem, UsernameChangeItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is UsernameChangeItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_feed_username_change)
        return ViewHolder(view, onUserClick)
    }

    override fun onBindViewHolder(item: UsernameChangeItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.headerView.time = elapsedTimeFormatter.format(item.date)
        holder.headerView.setUser(item.user, imageLoader)
        holder.descriptionView.text = HtmlCompat.fromHtml(holder.itemView.resources.getString(R.string.feed_username_change_description,
            item.formerName.htmlEncode()), 0)
    }

    class ViewHolder(
        itemView: View,
        onUserClick: OnUserClickListener
    ) : BaseAdapterDelegate.ViewHolder<UsernameChangeItem>(itemView) {
        val headerView: FeedHeaderView = itemView.findViewById(R.id.header)
        val descriptionView: TextView = itemView.findViewById(R.id.description)

        init {
            headerView.onUserClick = onUserClick
        }
    }
}

internal class FeedAdapter(
    imageLoader: ImageLoader,
    elapsedTimeFormatter: ElapsedTimeFormatter,
    gridItemSizeCallback: ItemSizeCallback,
    onCollectionFolderClick: OnCollectionFolderClickListener,
    onDeviationClick: OnDeviationClickListener,
    onStatusClick: OnStatusClickListener,
    onCommentsClick: OnCommentsClickListener,
    onFavoriteClick: OnFavoriteClickListener,
    onShareClick: OnShareClickListener,
    onUserClick: OnUserClickListener
) : ListDelegationAdapter<List<ListItem>>() {

    init {
        delegatesManager.addDelegate(
            CollectionUpdateItemDelegate(imageLoader, elapsedTimeFormatter, gridItemSizeCallback, onCollectionFolderClick, onDeviationClick, onUserClick))
        delegatesManager.addDelegate(
            ImageDeviationItemDelegate(imageLoader, elapsedTimeFormatter, onDeviationClick, onCommentsClick, onFavoriteClick, onShareClick, onUserClick))
        delegatesManager.addDelegate(
            LiteratureDeviationItemDelegate(imageLoader, elapsedTimeFormatter, onDeviationClick, onCommentsClick, onFavoriteClick, onShareClick, onUserClick))
        delegatesManager.addDelegate(
            MultipleDeviationsItemDelegate(imageLoader, elapsedTimeFormatter, gridItemSizeCallback, onDeviationClick, onUserClick))
        delegatesManager.addDelegate(
            StatusItemDelegate(imageLoader, elapsedTimeFormatter, onStatusClick, onDeviationClick, onCommentsClick, onShareClick, onUserClick))
        delegatesManager.addDelegate(UsernameChangeItemDelegate(imageLoader, elapsedTimeFormatter, onUserClick))
        delegatesManager.addDelegate(LoadingIndicatorItemDelegate())
    }
}

private class DeviationsAdapter(
    imageLoader: ImageLoader,
    itemSizeCallback: ItemSizeCallback,
    onDeviationClick: OnDeviationClickListener
) : ListDelegationAdapter<List<ListItem>>() {

    init {
        val layoutModeProvider = { LayoutMode.Grid }
        delegatesManager.addDelegate(GridImageDeviationItemDelegate(imageLoader, layoutModeProvider, itemSizeCallback, onDeviationClick))
        delegatesManager.addDelegate(GridLiteratureDeviationItemDelegate(layoutModeProvider, itemSizeCallback, onDeviationClick))
        delegatesManager.addDelegate(GridVideoDeviationItemDelegate(imageLoader, layoutModeProvider, itemSizeCallback, onDeviationClick))
    }

    fun update(items: List<ListItem>) {
        if (this.items != items) {
            this.items = items
            notifyDataSetChanged()
        }
    }
}
