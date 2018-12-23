package io.plastique.feed

import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
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
import io.plastique.glide.GlideApp
import io.plastique.statuses.ShareObjectId
import io.plastique.statuses.ShareUiModel
import io.plastique.statuses.isDeleted
import io.plastique.users.User

private class CollectionUpdateItemDelegate(
    private val itemSizeCallback: ItemSizeCallback,
    private val onDeviationClick: OnDeviationClickListener,
    private val onViewHolderClickListener: OnViewHolderClickListener
) : BaseAdapterDelegate<CollectionUpdateItem, ListItem, CollectionUpdateItemDelegate.ViewHolder>() {
    override fun isForViewType(item: ListItem): Boolean = item is CollectionUpdateItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feed_collection_update, parent, false)
        return ViewHolder(view, itemSizeCallback, onDeviationClick, onViewHolderClickListener)
    }

    override fun onBindViewHolder(item: CollectionUpdateItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.headerView.user = item.user
        holder.headerView.date = item.date
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
        itemSizeCallback: ItemSizeCallback,
        onDeviationClick: OnDeviationClickListener,
        private val onClickListener: OnViewHolderClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val headerView: FeedElementHeaderView = itemView.findViewById(R.id.header)
        val folderNameView: TextView = itemView.findViewById(R.id.folder_name)
        val folderItemsView: RecyclerView = itemView.findViewById(R.id.thumbnails)
        val folderItemsAdapter: DeviationsAdapter = DeviationsAdapter(folderItemsView.context, itemSizeCallback, onDeviationClick)
        val layoutManager: FlexboxLayoutManager = FlexboxLayoutManager(folderItemsView.context)

        init {
            headerView.setOnUserClickListener(this)
            folderNameView.setOnClickListener(this)
            folderItemsView.adapter = folderItemsAdapter
            folderItemsView.itemAnimator = DefaultItemAnimator().apply { supportsChangeAnimations = false }

            // TODO: Thumbnail click listener
        }

        override fun onClick(view: View) {
            onClickListener.onViewHolderClick(this, view)
        }
    }
}

private class ImageDeviationItemDelegate(private val onViewHolderClickListener: OnViewHolderClickListener) : BaseAdapterDelegate<ImageDeviationItem, ListItem, ImageDeviationItemDelegate.ViewHolder>() {
    override fun isForViewType(item: ListItem): Boolean = item is ImageDeviationItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feed_deviation_image, parent, false)
        return ViewHolder(view, onViewHolderClickListener)
    }

    override fun onBindViewHolder(item: ImageDeviationItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.headerView.user = item.user
        holder.headerView.date = item.date
        holder.titleView.text = item.deviation.title
        holder.commentsButton.text = item.deviation.stats.comments.toString()
        holder.commentsButton.isVisible = item.deviation.properties.allowsComments
        holder.favoriteButton.text = item.deviation.stats.favorites.toString()
        holder.favoriteButton.isChecked = item.deviation.properties.isFavorite

        val image = item.deviation.preview ?: item.deviation.content!!
        (holder.imageView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "${image.size.width}:${image.size.height}"

        GlideApp.with(holder.imageView)
                .load(image.url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imageView)
    }

    class ViewHolder(itemView: View, private val onClickListener: OnViewHolderClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val headerView: FeedElementHeaderView = itemView.findViewById(R.id.header)
        val imageView: ImageView = itemView.findViewById(R.id.deviation_image)
        val titleView: TextView = itemView.findViewById(R.id.deviation_title)
        val commentsButton: TextView = itemView.findViewById(R.id.button_comments)
        val favoriteButton: CheckedTextView = itemView.findViewById(R.id.button_favorite)
        private val shareButton: View = itemView.findViewById(R.id.button_share)

        init {
            headerView.setOnUserClickListener(this)
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

private class LiteratureDeviationItemDelegate(private val onViewHolderClickListener: OnViewHolderClickListener) : BaseAdapterDelegate<LiteratureDeviationItem, ListItem, LiteratureDeviationItemDelegate.ViewHolder>() {
    override fun isForViewType(item: ListItem): Boolean = item is LiteratureDeviationItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feed_deviation_literature, parent, false)
        return ViewHolder(view, onViewHolderClickListener)
    }

    override fun onBindViewHolder(item: LiteratureDeviationItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.headerView.user = item.user
        holder.headerView.date = item.date
        holder.titleView.text = item.deviation.title
        holder.excerptView.text = item.excerpt.value
        holder.commentsButton.text = item.deviation.stats.comments.toString()
        holder.commentsButton.isVisible = item.deviation.properties.allowsComments
        holder.favoriteButton.text = item.deviation.stats.favorites.toString()
        holder.favoriteButton.isChecked = item.deviation.properties.isFavorite
    }

    class ViewHolder(itemView: View, private val onClickListener: OnViewHolderClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val headerView: FeedElementHeaderView = itemView.findViewById(R.id.header)
        val titleView: TextView = itemView.findViewById(R.id.deviation_title)
        val excerptView: TextView = itemView.findViewById(R.id.deviation_excerpt)
        val commentsButton: TextView = itemView.findViewById(R.id.button_comments)
        val favoriteButton: CheckedTextView = itemView.findViewById(R.id.button_favorite)
        private val shareButton: View = itemView.findViewById(R.id.button_share)

        init {
            headerView.setOnUserClickListener(this)
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

private class MultipleDeviationsItemDelegate(
    private val gridItemSizeCallback: ItemSizeCallback,
    private val onDeviationClick: OnDeviationClickListener,
    private val onViewHolderClickListener: OnViewHolderClickListener
) : BaseAdapterDelegate<MultipleDeviationsItem, ListItem, MultipleDeviationsItemDelegate.ViewHolder>() {
    override fun isForViewType(item: ListItem): Boolean = item is MultipleDeviationsItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feed_multiple_deviations, parent, false)
        return ViewHolder(view, gridItemSizeCallback, onDeviationClick, onViewHolderClickListener)
    }

    override fun onBindViewHolder(item: MultipleDeviationsItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.headerView.user = item.user
        holder.headerView.date = item.date
        val resources = holder.itemView.resources
        holder.descriptionView.text = resources.getString(R.string.feed_multiple_deviations_submitted_description,
                resources.getQuantityString(R.plurals.feed_deviations, item.submittedTotal, item.submittedTotal))
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
        gridItemSizeCallback: ItemSizeCallback,
        onDeviationClick: OnDeviationClickListener,
        private val onClickListener: OnViewHolderClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val headerView: FeedElementHeaderView = itemView.findViewById(R.id.header)
        val descriptionView: TextView = itemView.findViewById(R.id.description)
        val deviationsView: RecyclerView = itemView.findViewById(R.id.deviations)
        val adapter: DeviationsAdapter = DeviationsAdapter(itemView.context, gridItemSizeCallback, onDeviationClick)
        val layoutManager: FlexboxLayoutManager = FlexboxLayoutManager(deviationsView.context)

        init {
            deviationsView.adapter = adapter
            deviationsView.itemAnimator = DefaultItemAnimator().apply { supportsChangeAnimations = false }
            headerView.setOnUserClickListener(this)
        }

        override fun onClick(view: View) {
            onClickListener.onViewHolderClick(this, view)
        }
    }
}

private class StatusItemDelegate(private val onViewHolderClickListener: OnViewHolderClickListener) : BaseAdapterDelegate<StatusUpdateItem, ListItem, StatusItemDelegate.ViewHolder>() {
    override fun isForViewType(item: ListItem): Boolean = item is StatusUpdateItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feed_status, parent, false)
        return ViewHolder(view, onViewHolderClickListener)
    }

    override fun onBindViewHolder(item: StatusUpdateItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.headerView.user = item.user
        holder.headerView.date = item.date
        holder.statusTextView.text = item.text.value
        holder.commentsButton.text = item.commentCount.toString()
        holder.shareButton.isVisible = !item.share.isDeleted

        holder.shareContainer.removeAllViews()
        holder.shareContainer.isVisible = item.share !== ShareUiModel.None
        holder.shareContainer.setOnClickListener(if (!item.share.isDeleted) holder else null)
        holder.shareContainer.setBackgroundResource(if (!item.share.isDeleted) R.drawable.feed_status_shared_object_background else R.drawable.feed_status_shared_object_deleted_background)
        renderShare(item.share, holder.shareContainer)
    }

    private fun renderShare(share: ShareUiModel, parent: ViewGroup) {
        when (share) {
            is ShareUiModel.ImageDeviation -> {
                val shareView = View.inflate(parent.context, R.layout.inc_feed_shared_deviation_image, parent)

                val headerView: FeedElementHeaderView = shareView.findViewById(R.id.header)
                val titleView: TextView = shareView.findViewById(R.id.deviation_title)
                val imageView: ImageView = shareView.findViewById(R.id.deviation_image)
                headerView.user = share.author
                titleView.text = share.title

                val preview = share.preview
                (imageView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "${preview.size.width}:${preview.size.height}"

                GlideApp.with(imageView)
                        .load(preview.url)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageView)
            }

            is ShareUiModel.LiteratureDeviation -> {
                val shareView = View.inflate(parent.context, R.layout.inc_feed_shared_deviation_literature, parent)
                val headerView: FeedElementHeaderView = shareView.findViewById(R.id.header)
                val titleView: TextView = shareView.findViewById(R.id.deviation_title)
                val excerptView: TextView = shareView.findViewById(R.id.deviation_excerpt)
                headerView.user = share.author
                titleView.text = share.title
                excerptView.text = share.excerpt.value
            }

            is ShareUiModel.Status -> {
                val shareView = View.inflate(parent.context, R.layout.inc_feed_shared_status, parent)
                val headerView: FeedElementHeaderView = shareView.findViewById(R.id.header)
                val statusTextView: TextView = shareView.findViewById(R.id.status_text)
                headerView.user = share.author
                headerView.date = share.date
                statusTextView.text = share.text.value
            }

            ShareUiModel.DeletedDeviation -> {
                val shareView = View.inflate(parent.context, R.layout.inc_feed_shared_object_deleted, parent)
                val textView: TextView = shareView.findViewById(R.id.text)
                textView.setText(R.string.feed_status_shared_deviation_deleted)
            }

            ShareUiModel.DeletedStatus -> {
                val shareView = View.inflate(parent.context, R.layout.inc_feed_shared_object_deleted, parent)
                val textView: TextView = shareView.findViewById(R.id.text)
                textView.setText(R.string.feed_status_shared_status_deleted)
            }
        }
    }

    class ViewHolder(itemView: View, private val onClickListener: OnViewHolderClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val headerView: FeedElementHeaderView = itemView.findViewById(R.id.header)
        val statusTextView: TextView = itemView.findViewById(R.id.status_text)
        val shareContainer: ViewGroup = itemView.findViewById(R.id.share_container)
        val commentsButton: TextView = itemView.findViewById(R.id.button_comments)
        val shareButton: View = itemView.findViewById(R.id.button_share)

        init {
            headerView.setOnUserClickListener(this)
            statusTextView.setOnClickListener(this)
            commentsButton.setOnClickListener(this)
            shareButton.setOnClickListener(this)

            statusTextView.movementMethod = LinkMovementMethod.getInstance()
        }

        override fun onClick(view: View) {
            onClickListener.onViewHolderClick(this, view)
        }
    }
}

private class UsernameChangeItemDelegate(private val onViewHolderClickListener: OnViewHolderClickListener) : BaseAdapterDelegate<UsernameChangeItem, ListItem, UsernameChangeItemDelegate.ViewHolder>() {
    override fun isForViewType(item: ListItem): Boolean = item is UsernameChangeItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feed_username_change, parent, false)
        return ViewHolder(view, onViewHolderClickListener)
    }

    override fun onBindViewHolder(item: UsernameChangeItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.headerView.user = item.user
        holder.headerView.date = item.date
        holder.descriptionView.text = HtmlCompat.fromHtml(holder.itemView.resources.getString(R.string.feed_username_change_description, TextUtils.htmlEncode(item.formerName)), 0)
    }

    private class ViewHolder(itemView: View, private val onClickListener: OnViewHolderClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val headerView: FeedElementHeaderView = itemView.findViewById(R.id.header)
        val descriptionView: TextView = itemView.findViewById(R.id.description)

        init {
            headerView.setOnUserClickListener(this)
        }

        override fun onClick(view: View) {
            onClickListener.onViewHolderClick(this, view)
        }
    }
}

class FeedAdapter(
    gridItemSizeCallback: ItemSizeCallback,
    private val onCollectionFolderClick: OnCollectionFolderClickListener,
    private val onCommentsClick: OnCommentsClickListener,
    private val onFavoriteClick: OnFavoriteClickListener,
    private val onDeviationClick: OnDeviationClickListener,
    private val onShareClick: OnShareClickListener,
    private val onStatusClick: OnStatusClickListener,
    private val onUserClick: OnUserClickListener
) : ListDelegationAdapter<List<ListItem>>(), OnViewHolderClickListener {

    init {
        delegatesManager.addDelegate(CollectionUpdateItemDelegate(gridItemSizeCallback, onDeviationClick, this))
        delegatesManager.addDelegate(ImageDeviationItemDelegate(this))
        delegatesManager.addDelegate(LiteratureDeviationItemDelegate(this))
        delegatesManager.addDelegate(MultipleDeviationsItemDelegate(gridItemSizeCallback, onDeviationClick, this))
        delegatesManager.addDelegate(StatusItemDelegate(this))
        delegatesManager.addDelegate(UsernameChangeItemDelegate(this))
        delegatesManager.addDelegate(LoadingIndicatorItemDelegate())
    }

    override fun onViewHolderClick(holder: RecyclerView.ViewHolder, view: View) {
        val position = holder.adapterPosition
        if (position == RecyclerView.NO_POSITION) return
        val item = items[position]

        when {
            view is FeedElementHeaderView -> onUserClick(view.user!!)
            view.id == R.id.button_comments -> {
                val threadId = when (item) {
                    is DeviationItem -> CommentThreadId.Deviation(item.deviation.id)
                    is StatusUpdateItem -> CommentThreadId.Status(item.statusId)
                    else -> throw IllegalStateException("Unexpected item ${item.javaClass}")
                }
                onCommentsClick(threadId)
            }
            view.id == R.id.button_favorite -> {
                val deviation = (item as DeviationItem).deviation
                onFavoriteClick(deviation.id, !deviation.properties.isFavorite)
            }
            view.id == R.id.button_share -> {
                val shareObjectId = getObjectToShare(item)
                onShareClick(shareObjectId)
            }
            view.id == R.id.deviation_image || view.id == R.id.deviation_title || view.id == R.id.deviation_excerpt -> {
                val deviation = (item as DeviationItem).deviation
                onDeviationClick(deviation.id)
            }
            view.id == R.id.status_text -> {
                val statusId = (item as StatusUpdateItem).statusId
                onStatusClick(statusId)
            }
            view.id == R.id.share_container -> {
                val share = (item as StatusUpdateItem).share
                when (share) {
                    is ShareUiModel.ImageDeviation -> onDeviationClick(share.deviationId)
                    is ShareUiModel.LiteratureDeviation -> onDeviationClick(share.deviationId)
                    is ShareUiModel.Status -> onStatusClick(share.statusId)
                    else -> throw IllegalStateException("Unexpected shared item $share")
                }
            }
            view.id == R.id.folder_name -> {
                val collectionUpdateItem = item as CollectionUpdateItem
                onCollectionFolderClick(collectionUpdateItem.user.name, collectionUpdateItem.folderId, collectionUpdateItem.folderName)
            }
        }
    }

    private fun getObjectToShare(item: ListItem): ShareObjectId = when (item) {
        is StatusUpdateItem -> when (item.share) {
            is ShareUiModel.None -> ShareObjectId.Status(item.statusId)
            is ShareUiModel.ImageDeviation -> ShareObjectId.Deviation(item.share.deviationId, item.statusId)
            is ShareUiModel.LiteratureDeviation -> ShareObjectId.Deviation(item.share.deviationId, item.statusId)
            is ShareUiModel.Status -> ShareObjectId.Status(item.share.statusId, item.statusId)
            else -> throw IllegalStateException("Nothing to share")
        }
        is DeviationItem -> ShareObjectId.Deviation(item.deviation.id)
        else -> throw IllegalStateException("Unexpected item ${item.javaClass}")
    }
}

typealias OnCollectionFolderClickListener = (username: String?, folderId: String, folderName: String) -> Unit
typealias OnCommentsClickListener = (threadId: CommentThreadId) -> Unit
typealias OnDeviationClickListener = (deviationId: String) -> Unit
typealias OnFavoriteClickListener = (deviationId: String, favorite: Boolean) -> Unit
typealias OnShareClickListener = (shareObjectId: ShareObjectId) -> Unit
typealias OnStatusClickListener = (statusId: String) -> Unit
typealias OnUserClickListener = (user: User) -> Unit
