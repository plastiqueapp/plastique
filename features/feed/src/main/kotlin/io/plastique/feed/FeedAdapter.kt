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
import io.plastique.comments.CommentThreadId
import io.plastique.common.FeedHeaderView
import io.plastique.common.OnUserClickListener
import io.plastique.core.image.ImageLoader
import io.plastique.core.image.TransformType
import io.plastique.core.lists.BaseAdapterDelegate
import io.plastique.core.lists.ItemSizeCallback
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItemDelegate
import io.plastique.core.lists.OnViewHolderClickListener
import io.plastique.core.text.RichTextView
import io.plastique.core.time.ElapsedTimeFormatter
import io.plastique.deviations.DeviationActionsView
import io.plastique.deviations.list.GridImageDeviationItemDelegate
import io.plastique.deviations.list.GridLiteratureDeviationItemDelegate
import io.plastique.deviations.list.GridVideoDeviationItemDelegate
import io.plastique.deviations.list.ImageHelper
import io.plastique.deviations.list.LayoutMode
import io.plastique.statuses.ShareObjectId
import io.plastique.statuses.ShareUiModel
import io.plastique.statuses.ShareView
import io.plastique.statuses.StatusActionsView
import io.plastique.statuses.isDeleted
import io.plastique.util.dimensionRatio
import io.plastique.deviations.list.DeviationItem as InnerDeviationItem

private class CollectionUpdateItemDelegate(
    private val imageLoader: ImageLoader,
    private val elapsedTimeFormatter: ElapsedTimeFormatter,
    private val itemSizeCallback: ItemSizeCallback,
    private val onDeviationClick: OnDeviationClickListener,
    private val onUserClick: OnUserClickListener,
    private val onViewHolderClickListener: OnViewHolderClickListener
) : BaseAdapterDelegate<CollectionUpdateItem, ListItem, CollectionUpdateItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is CollectionUpdateItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_feed_collection_update)
        return ViewHolder(view, imageLoader, itemSizeCallback, onDeviationClick, onUserClick, onViewHolderClickListener)
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
        onDeviationClick: OnDeviationClickListener,
        onUserClick: OnUserClickListener,
        private val onClickListener: OnViewHolderClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val headerView: FeedHeaderView = itemView.findViewById(R.id.header)
        val folderNameView: TextView = itemView.findViewById(R.id.folder_name)
        val folderItemsView: RecyclerView = itemView.findViewById(R.id.thumbnails)
        val folderItemsAdapter: DeviationsAdapter = DeviationsAdapter(imageLoader, itemSizeCallback, onDeviationClick)
        val layoutManager: FlexboxLayoutManager = FlexboxLayoutManager(folderItemsView.context)

        init {
            headerView.onUserClickListener = onUserClick
            folderNameView.setOnClickListener(this)
            folderItemsView.adapter = folderItemsAdapter
            folderItemsView.disableChangeAnimations()

            // TODO: Thumbnail click listener
        }

        override fun onClick(view: View) {
            onClickListener.onViewHolderClick(this, view)
        }
    }
}

private class ImageDeviationItemDelegate(
    private val imageLoader: ImageLoader,
    private val elapsedTimeFormatter: ElapsedTimeFormatter,
    private val onUserClick: OnUserClickListener,
    private val onViewHolderClickListener: OnViewHolderClickListener
) : BaseAdapterDelegate<ImageDeviationItem, ListItem, ImageDeviationItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is ImageDeviationItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_feed_deviation_image)
        return ViewHolder(view, onUserClick, onViewHolderClickListener, ImageHelper.getMaxWidth(parent))
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
        onUserClick: OnUserClickListener,
        private val onClickListener: OnViewHolderClickListener,
        val maxImageWidth: Int
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val headerView: FeedHeaderView = itemView.findViewById(R.id.header)
        val previewView: ImageView = itemView.findViewById(R.id.deviation_preview)
        val titleView: TextView = itemView.findViewById(R.id.deviation_title)
        val actionsView: DeviationActionsView = itemView.findViewById(R.id.deviation_actions)

        init {
            headerView.onUserClickListener = onUserClick
            previewView.setOnClickListener(this)
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

private class LiteratureDeviationItemDelegate(
    private val imageLoader: ImageLoader,
    private val elapsedTimeFormatter: ElapsedTimeFormatter,
    private val onUserClick: OnUserClickListener,
    private val onViewHolderClickListener: OnViewHolderClickListener
) : BaseAdapterDelegate<LiteratureDeviationItem, ListItem, LiteratureDeviationItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is LiteratureDeviationItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_feed_deviation_literature)
        return ViewHolder(view, onUserClick, onViewHolderClickListener)
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
        onUserClick: OnUserClickListener,
        private val onClickListener: OnViewHolderClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val headerView: FeedHeaderView = itemView.findViewById(R.id.header)
        val titleView: TextView = itemView.findViewById(R.id.deviation_title)
        val excerptView: RichTextView = itemView.findViewById(R.id.deviation_excerpt)
        val actionsView: DeviationActionsView = itemView.findViewById(R.id.deviation_actions)

        init {
            headerView.onUserClickListener = onUserClick
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
    ) : RecyclerView.ViewHolder(itemView) {
        val headerView: FeedHeaderView = itemView.findViewById(R.id.header)
        val descriptionView: TextView = itemView.findViewById(R.id.description)
        val deviationsView: RecyclerView = itemView.findViewById(R.id.deviations)
        val adapter: DeviationsAdapter = DeviationsAdapter(imageLoader, gridItemSizeCallback, onDeviationClick)
        val layoutManager: FlexboxLayoutManager = FlexboxLayoutManager(deviationsView.context)

        init {
            headerView.onUserClickListener = onUserClick
            deviationsView.adapter = adapter
            deviationsView.disableChangeAnimations()
        }
    }
}

private class StatusItemDelegate(
    private val imageLoader: ImageLoader,
    private val elapsedTimeFormatter: ElapsedTimeFormatter,
    private val onUserClick: OnUserClickListener,
    private val onViewHolderClickListener: OnViewHolderClickListener
) : BaseAdapterDelegate<StatusUpdateItem, ListItem, StatusItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is StatusUpdateItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_feed_status)
        return ViewHolder(view, onUserClick, onViewHolderClickListener)
    }

    override fun onBindViewHolder(item: StatusUpdateItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.headerView.time = elapsedTimeFormatter.format(item.date)
        holder.headerView.setUser(item.user, imageLoader)
        holder.statusTextView.text = item.text.value
        holder.actionsView.render(item.actionsState)

        holder.shareView.setShare(item.share, imageLoader, elapsedTimeFormatter)
        holder.shareView.setOnClickListener(if (!item.share.isDeleted) holder else null)
    }

    class ViewHolder(
        itemView: View,
        onUserClick: OnUserClickListener,
        private val onClickListener: OnViewHolderClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val headerView: FeedHeaderView = itemView.findViewById(R.id.header)
        val statusTextView: RichTextView = itemView.findViewById(R.id.status_text)
        val shareView: ShareView = itemView.findViewById(R.id.status_share)
        val actionsView: StatusActionsView = itemView.findViewById(R.id.status_actions)

        init {
            headerView.onUserClickListener = onUserClick
            statusTextView.setOnClickListener(this)
            actionsView.setOnCommentsClickListener(this)
            actionsView.setOnShareClickListener(this)

            statusTextView.movementMethod = LinkMovementMethod.getInstance()
        }

        override fun onClick(view: View) {
            onClickListener.onViewHolderClick(this, view)
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
    ) : RecyclerView.ViewHolder(itemView) {
        val headerView: FeedHeaderView = itemView.findViewById(R.id.header)
        val descriptionView: TextView = itemView.findViewById(R.id.description)

        init {
            headerView.onUserClickListener = onUserClick
        }
    }
}

internal class FeedAdapter(
    imageLoader: ImageLoader,
    elapsedTimeFormatter: ElapsedTimeFormatter,
    gridItemSizeCallback: ItemSizeCallback,
    private val onCollectionFolderClick: OnCollectionFolderClickListener,
    private val onCommentsClick: OnCommentsClickListener,
    private val onFavoriteClick: OnFavoriteClickListener,
    private val onDeviationClick: OnDeviationClickListener,
    private val onShareClick: OnShareClickListener,
    private val onStatusClick: OnStatusClickListener,
    onUserClick: OnUserClickListener
) : ListDelegationAdapter<List<ListItem>>(), OnViewHolderClickListener {

    init {
        delegatesManager.addDelegate(CollectionUpdateItemDelegate(imageLoader, elapsedTimeFormatter, gridItemSizeCallback, onDeviationClick, onUserClick, this))
        delegatesManager.addDelegate(ImageDeviationItemDelegate(imageLoader, elapsedTimeFormatter, onUserClick, this))
        delegatesManager.addDelegate(LiteratureDeviationItemDelegate(imageLoader, elapsedTimeFormatter, onUserClick, this))
        delegatesManager.addDelegate(MultipleDeviationsItemDelegate(imageLoader, elapsedTimeFormatter, gridItemSizeCallback, onDeviationClick, onUserClick))
        delegatesManager.addDelegate(StatusItemDelegate(imageLoader, elapsedTimeFormatter, onUserClick, this))
        delegatesManager.addDelegate(UsernameChangeItemDelegate(imageLoader, elapsedTimeFormatter, onUserClick))
        delegatesManager.addDelegate(LoadingIndicatorItemDelegate())
    }

    @Suppress("ComplexMethod")
    override fun onViewHolderClick(holder: RecyclerView.ViewHolder, view: View) {
        val position = holder.adapterPosition
        if (position == RecyclerView.NO_POSITION) return
        val item = items[position]
        when (view.id) {
            R.id.deviation_actions_comments -> {
                require(item is DeviationItem)
                onCommentsClick(CommentThreadId.Deviation(item.deviationId))
            }
            R.id.deviation_actions_favorite -> {
                require(item is DeviationItem)
                onFavoriteClick(item.id, !item.actionsState.isFavorite)
            }
            R.id.deviation_actions_share -> {
                require(item is DeviationItem)
                onShareClick(ShareObjectId.Deviation(item.deviationId))
            }
            R.id.status_actions_comments -> {
                require(item is StatusUpdateItem)
                onCommentsClick(CommentThreadId.Status(item.statusId))
            }
            R.id.status_actions_share -> {
                require(item is StatusUpdateItem)
                onShareClick(item.shareObjectId)
            }
            R.id.deviation_preview, R.id.deviation_title, R.id.deviation_excerpt -> {
                require(item is DeviationItem)
                onDeviationClick(item.deviationId)
            }
            R.id.status_text -> {
                val statusId = (item as StatusUpdateItem).statusId
                onStatusClick(statusId)
            }
            R.id.status_share -> {
                require(item is StatusUpdateItem)
                when (val share = item.share) {
                    is ShareUiModel.ImageDeviation -> onDeviationClick(share.deviationId)
                    is ShareUiModel.LiteratureDeviation -> onDeviationClick(share.deviationId)
                    is ShareUiModel.Status -> onStatusClick(share.statusId)
                    else -> throw IllegalStateException("Unexpected share $share")
                }
            }
            R.id.folder_name -> {
                require(item is CollectionUpdateItem)
                onCollectionFolderClick(item.user.name, item.folderId, item.folderName)
            }
        }
    }

    private val StatusUpdateItem.shareObjectId: ShareObjectId
        get() = when (share) {
            is ShareUiModel.None -> ShareObjectId.Status(statusId)
            is ShareUiModel.ImageDeviation -> ShareObjectId.Deviation(share.deviationId, statusId)
            is ShareUiModel.LiteratureDeviation -> ShareObjectId.Deviation(share.deviationId, statusId)
            is ShareUiModel.Status -> ShareObjectId.Status(share.statusId, statusId)
            else -> throw IllegalStateException("Nothing to share")
        }
}

private class DeviationsAdapter(
    imageLoader: ImageLoader,
    itemSizeCallback: ItemSizeCallback,
    private val onDeviationClick: OnDeviationClickListener
) : ListDelegationAdapter<List<ListItem>>(), OnViewHolderClickListener {

    init {
        val layoutModeProvider = { LayoutMode.Grid }
        delegatesManager.addDelegate(GridImageDeviationItemDelegate(imageLoader, layoutModeProvider, itemSizeCallback, this))
        delegatesManager.addDelegate(GridLiteratureDeviationItemDelegate(layoutModeProvider, itemSizeCallback, this))
        delegatesManager.addDelegate(GridVideoDeviationItemDelegate(imageLoader, layoutModeProvider, itemSizeCallback, this))
    }

    fun update(items: List<ListItem>) {
        if (this.items != items) {
            this.items = items
            notifyDataSetChanged()
        }
    }

    override fun onViewHolderClick(holder: RecyclerView.ViewHolder, view: View) {
        val position = holder.adapterPosition
        if (position != RecyclerView.NO_POSITION) {
            val item = items[position] as InnerDeviationItem
            onDeviationClick(item.deviationId)
        }
    }
}

private typealias OnCollectionFolderClickListener = (username: String?, folderId: String, folderName: String) -> Unit
private typealias OnCommentsClickListener = (threadId: CommentThreadId) -> Unit
private typealias OnDeviationClickListener = (deviationId: String) -> Unit
private typealias OnFavoriteClickListener = (deviationId: String, favorite: Boolean) -> Unit
private typealias OnShareClickListener = (shareObjectId: ShareObjectId) -> Unit
private typealias OnStatusClickListener = (statusId: String) -> Unit
