package io.plastique.feed

import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.core.text.htmlEncode
import androidx.recyclerview.widget.RecyclerView
import com.github.technoir42.android.extensions.disableChangeAnimations
import com.github.technoir42.android.extensions.layoutInflater
import com.google.android.flexbox.FlexboxLayoutManager
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.collections.OnCollectionFolderClickListener
import io.plastique.comments.OnCommentsClickListener
import io.plastique.core.image.ImageLoader
import io.plastique.core.image.TransformType
import io.plastique.core.lists.BaseAdapterDelegate
import io.plastique.core.lists.ItemSizeCallback
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItemDelegate
import io.plastique.core.time.ElapsedTimeFormatter
import io.plastique.deviations.OnDeviationClickListener
import io.plastique.deviations.OnFavoriteClickListener
import io.plastique.deviations.list.GridImageDeviationItemDelegate
import io.plastique.deviations.list.GridLiteratureDeviationItemDelegate
import io.plastique.deviations.list.GridVideoDeviationItemDelegate
import io.plastique.deviations.list.ImageHelper
import io.plastique.deviations.list.LayoutMode
import io.plastique.feed.databinding.ItemFeedCollectionUpdateBinding
import io.plastique.feed.databinding.ItemFeedDeviationImageBinding
import io.plastique.feed.databinding.ItemFeedDeviationLiteratureBinding
import io.plastique.feed.databinding.ItemFeedMultipleDeviationsBinding
import io.plastique.feed.databinding.ItemFeedStatusBinding
import io.plastique.feed.databinding.ItemFeedUsernameChangeBinding
import io.plastique.statuses.OnShareClickListener
import io.plastique.statuses.OnStatusClickListener
import io.plastique.statuses.share.ShareUiModel
import io.plastique.statuses.share.isDeleted
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
        val binding = ItemFeedCollectionUpdateBinding.inflate(parent.layoutInflater, parent, false)
        return ViewHolder(binding, imageLoader, itemSizeCallback, onCollectionFolderClick, onDeviationClick, onUserClick)
    }

    override fun onBindViewHolder(item: CollectionUpdateItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        val resources = holder.binding.root.resources
        holder.binding.header.time = elapsedTimeFormatter.format(item.date)
        holder.binding.header.setUser(item.user, imageLoader)
        holder.binding.folderName.text = resources.getString(R.string.feed_collection_folder_name, item.folderName, item.addedCount)
        holder.deviationsAdapter.update(item.folderItems)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        (holder as ViewHolder).run { holder.binding.deviations.layoutManager = layoutManager }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        (holder as ViewHolder).run { holder.binding.deviations.layoutManager = null }
    }

    class ViewHolder(
        val binding: ItemFeedCollectionUpdateBinding,
        imageLoader: ImageLoader,
        itemSizeCallback: ItemSizeCallback,
        onCollectionFolderClick: OnCollectionFolderClickListener,
        onDeviationClick: OnDeviationClickListener,
        onUserClick: OnUserClickListener
    ) : BaseAdapterDelegate.ViewHolder<CollectionUpdateItem>(binding.root) {
        val deviationsAdapter = DeviationsAdapter(imageLoader, itemSizeCallback, onDeviationClick)
        val layoutManager = FlexboxLayoutManager(binding.deviations.context)

        init {
            binding.header.onUserClick = onUserClick
            binding.folderName.setOnClickListener { onCollectionFolderClick(item.folderId, item.folderName) }
            binding.deviations.apply {
                adapter = deviationsAdapter
                disableChangeAnimations()
            }

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
        val binding = ItemFeedDeviationImageBinding.inflate(parent.layoutInflater, parent, false)
        return ViewHolder(binding, onDeviationClick, onCommentsClick, onFavoriteClick, onShareClick, onUserClick, ImageHelper.getMaxWidth(parent))
    }

    override fun onBindViewHolder(item: ImageDeviationItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.binding.header.time = elapsedTimeFormatter.format(item.date)
        holder.binding.header.setUser(item.user, imageLoader)
        holder.binding.title.text = item.title
        holder.binding.actions.render(item.actionsState)

        val preview = ImageHelper.choosePreview(item.preview, item.content, holder.maxImageWidth)
        val previewSize = ImageHelper.calculateOptimalPreviewSize(preview, holder.maxImageWidth)
        (holder.binding.preview.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = previewSize.dimensionRatio

        imageLoader.load(preview.url)
            .params {
                size = previewSize
                transforms += TransformType.CenterCrop
                cacheSource = true
            }
            .into(holder.binding.preview)
    }

    class ViewHolder(
        val binding: ItemFeedDeviationImageBinding,
        onDeviationClick: OnDeviationClickListener,
        onCommentsClick: OnCommentsClickListener,
        onFavoriteClick: OnFavoriteClickListener,
        onShareClick: OnShareClickListener,
        onUserClick: OnUserClickListener,
        val maxImageWidth: Int
    ) : BaseAdapterDelegate.ViewHolder<ImageDeviationItem>(binding.root) {

        init {
            val onDeviationClickListener = View.OnClickListener { onDeviationClick(item.deviationId) }
            binding.preview.setOnClickListener(onDeviationClickListener)
            binding.title.setOnClickListener(onDeviationClickListener)
            binding.header.onUserClick = onUserClick
            binding.actions.onCommentsClick = onCommentsClick
            binding.actions.onFavoriteClick = onFavoriteClick
            binding.actions.onShareClick = onShareClick
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
        val binding = ItemFeedDeviationLiteratureBinding.inflate(parent.layoutInflater, parent, false)
        return ViewHolder(binding, onDeviationClick, onCommentsClick, onFavoriteClick, onShareClick, onUserClick)
    }

    override fun onBindViewHolder(item: LiteratureDeviationItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.binding.header.time = elapsedTimeFormatter.format(item.date)
        holder.binding.header.setUser(item.user, imageLoader)
        holder.binding.title.text = item.title
        holder.binding.excerpt.text = item.excerpt.value
        holder.binding.actions.render(item.actionsState)
    }

    class ViewHolder(
        val binding: ItemFeedDeviationLiteratureBinding,
        onDeviationClick: OnDeviationClickListener,
        onCommentsClick: OnCommentsClickListener,
        onFavoriteClick: OnFavoriteClickListener,
        onShareClick: OnShareClickListener,
        onUserClick: OnUserClickListener
    ) : BaseAdapterDelegate.ViewHolder<LiteratureDeviationItem>(binding.root) {

        init {
            val onDeviationClickListener = View.OnClickListener { onDeviationClick(item.deviationId) }
            binding.title.setOnClickListener(onDeviationClickListener)
            binding.excerpt.setOnClickListener(onDeviationClickListener)
            binding.header.onUserClick = onUserClick
            binding.actions.onCommentsClick = onCommentsClick
            binding.actions.onFavoriteClick = onFavoriteClick
            binding.actions.onShareClick = onShareClick
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
        val binding = ItemFeedMultipleDeviationsBinding.inflate(parent.layoutInflater, parent, false)
        return ViewHolder(binding, imageLoader, gridItemSizeCallback, onDeviationClick, onUserClick)
    }

    override fun onBindViewHolder(item: MultipleDeviationsItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        val resources = holder.binding.root.resources
        holder.binding.header.time = elapsedTimeFormatter.format(item.date)
        holder.binding.header.setUser(item.user, imageLoader)
        holder.binding.description.text = resources.getString(R.string.feed_multiple_deviations_submitted_description,
            resources.getQuantityString(R.plurals.common_deviations, item.submittedTotal, item.submittedTotal))
        holder.deviationsAdapter.update(item.items)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        (holder as ViewHolder).run { binding.deviations.layoutManager = holder.layoutManager }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        (holder as ViewHolder).run { binding.deviations.layoutManager = null }
    }

    class ViewHolder(
        val binding: ItemFeedMultipleDeviationsBinding,
        imageLoader: ImageLoader,
        gridItemSizeCallback: ItemSizeCallback,
        onDeviationClick: OnDeviationClickListener,
        onUserClick: OnUserClickListener
    ) : BaseAdapterDelegate.ViewHolder<MultipleDeviationsItem>(binding.root) {
        val deviationsAdapter: DeviationsAdapter = DeviationsAdapter(imageLoader, gridItemSizeCallback, onDeviationClick)
        val layoutManager: FlexboxLayoutManager = FlexboxLayoutManager(binding.deviations.context)

        init {
            binding.header.onUserClick = onUserClick
            binding.deviations.apply {
                adapter = deviationsAdapter
                disableChangeAnimations()
            }
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
        val binding = ItemFeedStatusBinding.inflate(parent.layoutInflater, parent, false)
        return ViewHolder(binding, onStatusClick, onDeviationClick, onCommentsClick, onShareClick, onUserClick)
    }

    override fun onBindViewHolder(item: StatusUpdateItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.binding.header.time = elapsedTimeFormatter.format(item.date)
        holder.binding.header.setUser(item.user, imageLoader)
        holder.binding.text.text = item.text.value
        holder.binding.actions.render(item.actionsState)

        holder.binding.share.setShare(item.share, imageLoader, elapsedTimeFormatter)
        holder.binding.share.isClickable = !item.share.isDeleted
    }

    class ViewHolder(
        val binding: ItemFeedStatusBinding,
        onStatusClick: OnStatusClickListener,
        onDeviationClick: OnDeviationClickListener,
        onCommentsClick: OnCommentsClickListener,
        onShareClick: OnShareClickListener,
        onUserClick: OnUserClickListener
    ) : BaseAdapterDelegate.ViewHolder<StatusUpdateItem>(binding.root) {

        init {
            binding.text.setOnClickListener { onStatusClick(item.statusId) }
            binding.text.movementMethod = LinkMovementMethod.getInstance()
            binding.share.setOnClickListener {
                when (val share = item.share) {
                    is ShareUiModel.ImageDeviation -> onDeviationClick(share.deviationId)
                    is ShareUiModel.LiteratureDeviation -> onDeviationClick(share.deviationId)
                    is ShareUiModel.Status -> onStatusClick(share.statusId)
                }
            }
            binding.header.onUserClick = onUserClick
            binding.actions.onCommentsClick = onCommentsClick
            binding.actions.onShareClick = onShareClick
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
        val binding = ItemFeedUsernameChangeBinding.inflate(parent.layoutInflater, parent, false)
        return ViewHolder(binding, onUserClick)
    }

    override fun onBindViewHolder(item: UsernameChangeItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        val resources = holder.binding.root.resources
        holder.binding.header.time = elapsedTimeFormatter.format(item.date)
        holder.binding.header.setUser(item.user, imageLoader)
        holder.binding.description.text = HtmlCompat.fromHtml(resources.getString(R.string.feed_username_change_description, item.formerName.htmlEncode()), 0)
    }

    class ViewHolder(
        val binding: ItemFeedUsernameChangeBinding,
        onUserClick: OnUserClickListener
    ) : BaseAdapterDelegate.ViewHolder<UsernameChangeItem>(binding.root) {

        init {
            binding.header.onUserClick = onUserClick
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
