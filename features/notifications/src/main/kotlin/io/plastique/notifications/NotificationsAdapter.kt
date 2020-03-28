package io.plastique.notifications

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.github.technoir42.android.extensions.inflate
import com.github.technoir42.android.extensions.isStrikethrough
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.collections.OnCollectionFolderClickListener
import io.plastique.core.image.ImageLoader
import io.plastique.core.image.TransformType
import io.plastique.core.lists.BaseAdapterDelegate
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItemDelegate
import io.plastique.core.time.ElapsedTimeFormatter
import io.plastique.deviations.OnDeviationClickListener
import io.plastique.statuses.OnStatusClickListener
import io.plastique.users.OnUserClickListener
import io.plastique.users.UserType

private class AddToCollectionItemDelegate(
    private val imageLoader: ImageLoader,
    private val elapsedTimeFormatter: ElapsedTimeFormatter,
    private val onCollectionFolderClick: OnCollectionFolderClickListener
) : BaseAdapterDelegate<AddToCollectionItem, ListItem, AddToCollectionItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is AddToCollectionItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_notifications_simple)
        return ViewHolder(view, onCollectionFolderClick)
    }

    override fun onBindViewHolder(item: AddToCollectionItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.avatarView.contentDescription =
            holder.itemView.resources.getString(R.string.common_avatar_description, item.user.name)
        holder.usernameView.text = item.user.name
        holder.usernameView.isStrikethrough = item.user.type == UserType.Banned
        holder.timeView.text = elapsedTimeFormatter.format(item.time)
        holder.descriptionView.text = HtmlCompat.fromHtml(holder.itemView.resources.getString(R.string.notifications_item_add_to_collection,
            item.deviationTitle, item.folderName), 0)

        imageLoader.load(item.user.avatarUrl)
            .params {
                fallbackDrawable = R.drawable.default_avatar_64dp
                transforms += TransformType.CircleCrop
            }
            .into(holder.avatarView)
    }

    class ViewHolder(
        itemView: View,
        onCollectionFolderClick: OnCollectionFolderClickListener
    ) : BaseAdapterDelegate.ViewHolder<AddToCollectionItem>(itemView) {
        val avatarView: ImageView = itemView.findViewById(R.id.avatar)
        val usernameView: TextView = itemView.findViewById(R.id.username)
        val timeView: TextView = itemView.findViewById(R.id.time)
        val descriptionView: TextView = itemView.findViewById(R.id.description)

        init {
            itemView.setOnClickListener { onCollectionFolderClick(item.folderId, item.folderName) }
        }
    }
}

private class BadgeGivenItemDelegate(
    private val imageLoader: ImageLoader,
    private val elapsedTimeFormatter: ElapsedTimeFormatter,
    private val onUserClick: OnUserClickListener
) : BaseAdapterDelegate<BadgeGivenItem, ListItem, BadgeGivenItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is BadgeGivenItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_notifications_simple)
        return ViewHolder(view, onUserClick)
    }

    override fun onBindViewHolder(item: BadgeGivenItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.avatarView.contentDescription =
            holder.itemView.resources.getString(R.string.common_avatar_description, item.user.name)
        holder.usernameView.text = item.user.name
        holder.usernameView.isStrikethrough = item.user.type == UserType.Banned
        holder.timeView.text = elapsedTimeFormatter.format(item.time)
        holder.descriptionView.text = item.text

        imageLoader.load(item.user.avatarUrl)
            .params {
                fallbackDrawable = R.drawable.default_avatar_64dp
                transforms += TransformType.CircleCrop
            }
            .into(holder.avatarView)
    }

    class ViewHolder(
        itemView: View,
        onUserClick: OnUserClickListener
    ) : BaseAdapterDelegate.ViewHolder<BadgeGivenItem>(itemView) {
        val avatarView: ImageView = itemView.findViewById(R.id.avatar)
        val usernameView: TextView = itemView.findViewById(R.id.username)
        val timeView: TextView = itemView.findViewById(R.id.time)
        val descriptionView: TextView = itemView.findViewById(R.id.description)

        init {
            itemView.setOnClickListener { onUserClick(item.user) }
        }
    }
}

private class FavoriteItemDelegate(
    private val imageLoader: ImageLoader,
    private val elapsedTimeFormatter: ElapsedTimeFormatter,
    private val onUserClick: OnUserClickListener
) : BaseAdapterDelegate<FavoriteItem, ListItem, FavoriteItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is FavoriteItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_notifications_simple)
        return ViewHolder(view, onUserClick)
    }

    override fun onBindViewHolder(item: FavoriteItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.avatarView.contentDescription =
            holder.itemView.resources.getString(R.string.common_avatar_description, item.user.name)
        holder.usernameView.text = item.user.name
        holder.usernameView.isStrikethrough = item.user.type == UserType.Banned
        holder.timeView.text = elapsedTimeFormatter.format(item.time)
        holder.descriptionView.text = HtmlCompat.fromHtml(holder.itemView.resources.getString(R.string.notifications_item_favorite, item.deviationTitle), 0)

        imageLoader.load(item.user.avatarUrl)
            .params {
                fallbackDrawable = R.drawable.default_avatar_64dp
                transforms += TransformType.CircleCrop
            }
            .into(holder.avatarView)
    }

    class ViewHolder(
        itemView: View,
        onUserClick: OnUserClickListener
    ) : BaseAdapterDelegate.ViewHolder<FavoriteItem>(itemView) {
        val avatarView: ImageView = itemView.findViewById(R.id.avatar)
        val usernameView: TextView = itemView.findViewById(R.id.username)
        val timeView: TextView = itemView.findViewById(R.id.time)
        val descriptionView: TextView = itemView.findViewById(R.id.description)

        init {
            itemView.setOnClickListener { onUserClick(item.user) }
        }
    }
}

private class WatchItemDelegate(
    private val imageLoader: ImageLoader,
    private val elapsedTimeFormatter: ElapsedTimeFormatter,
    private val onUserClick: OnUserClickListener
) : BaseAdapterDelegate<WatchItem, ListItem, WatchItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is WatchItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_notifications_simple)
        return ViewHolder(view, onUserClick)
    }

    override fun onBindViewHolder(item: WatchItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.avatarView.contentDescription =
            holder.itemView.resources.getString(R.string.common_avatar_description, item.user.name)
        holder.usernameView.text = item.user.name
        holder.usernameView.isStrikethrough = item.user.type == UserType.Banned
        holder.timeView.text = elapsedTimeFormatter.format(item.time)
        holder.descriptionView.text = holder.itemView.resources.getString(R.string.notifications_item_watch)

        imageLoader.load(item.user.avatarUrl)
            .params {
                fallbackDrawable = R.drawable.default_avatar_64dp
                transforms += TransformType.CircleCrop
            }
            .into(holder.avatarView)
    }

    class ViewHolder(
        itemView: View,
        onUserClick: OnUserClickListener
    ) : BaseAdapterDelegate.ViewHolder<WatchItem>(itemView) {
        val avatarView: ImageView = itemView.findViewById(R.id.avatar)
        val usernameView: TextView = itemView.findViewById(R.id.username)
        val timeView: TextView = itemView.findViewById(R.id.time)
        val descriptionView: TextView = itemView.findViewById(R.id.description)

        init {
            itemView.setOnClickListener { onUserClick(item.user) }
        }
    }
}

internal class NotificationsAdapter(
    imageLoader: ImageLoader,
    elapsedTimeFormatter: ElapsedTimeFormatter,
    onCollectionFolderClick: OnCollectionFolderClickListener,
    onCommentClick: OnCommentClickListener,
    onDeviationClick: OnDeviationClickListener,
    onStatusClick: OnStatusClickListener,
    onUserClick: OnUserClickListener
) : ListDelegationAdapter<List<ListItem>>() {

    init {
        delegatesManager.addDelegate(LoadingIndicatorItemDelegate.VIEW_TYPE, LoadingIndicatorItemDelegate())
        delegatesManager.addDelegate(AddToCollectionItemDelegate(imageLoader, elapsedTimeFormatter, onCollectionFolderClick))
        delegatesManager.addDelegate(BadgeGivenItemDelegate(imageLoader, elapsedTimeFormatter, onUserClick))
        delegatesManager.addDelegate(FavoriteItemDelegate(imageLoader, elapsedTimeFormatter, onUserClick))
        delegatesManager.addDelegate(WatchItemDelegate(imageLoader, elapsedTimeFormatter, onUserClick))
    }
}

private typealias OnCommentClickListener = (commentId: String) -> Unit
