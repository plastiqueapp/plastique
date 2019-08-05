package io.plastique.notifications

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.technoir42.android.extensions.inflate
import com.github.technoir42.android.extensions.isStrikethrough
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.core.lists.BaseAdapterDelegate
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItemDelegate
import io.plastique.core.lists.OnViewHolderClickListener
import io.plastique.core.time.ElapsedTimeFormatter
import io.plastique.glide.GlideRequests
import io.plastique.users.User
import io.plastique.users.UserType

private class AddToCollectionItemDelegate(
    private val glide: GlideRequests,
    private val elapsedTimeFormatter: ElapsedTimeFormatter,
    private val onViewHolderClickListener: OnViewHolderClickListener
) : BaseAdapterDelegate<AddToCollectionItem, ListItem, AddToCollectionItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is AddToCollectionItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_notifications_simple)
        return ViewHolder(view, onViewHolderClickListener)
    }

    override fun onBindViewHolder(item: AddToCollectionItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.usernameView.text = item.user.name
        holder.usernameView.isStrikethrough = item.user.type == UserType.Banned
        holder.timeView.text = elapsedTimeFormatter.format(item.time)
        holder.descriptionView.text = HtmlCompat.fromHtml(holder.itemView.resources.getString(R.string.notifications_item_add_to_collection,
            item.deviationTitle, item.folderName), 0)

        glide.load(item.user.avatarUrl)
            .fallback(R.drawable.default_avatar_64dp)
            .circleCrop()
            .dontAnimate()
            .into(holder.avatarView)
    }

    class ViewHolder(itemView: View, private val onClickListener: OnViewHolderClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val avatarView: ImageView = itemView.findViewById(R.id.avatar)
        val usernameView: TextView = itemView.findViewById(R.id.username)
        val timeView: TextView = itemView.findViewById(R.id.time)
        val descriptionView: TextView = itemView.findViewById(R.id.description)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            onClickListener.onViewHolderClick(this, view)
        }
    }
}

private class BadgeGivenItemDelegate(
    private val glide: GlideRequests,
    private val elapsedTimeFormatter: ElapsedTimeFormatter,
    private val onViewHolderClickListener: OnViewHolderClickListener
) : BaseAdapterDelegate<BadgeGivenItem, ListItem, BadgeGivenItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is BadgeGivenItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_notifications_simple)
        return ViewHolder(view, onViewHolderClickListener)
    }

    override fun onBindViewHolder(item: BadgeGivenItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.usernameView.text = item.user.name
        holder.usernameView.isStrikethrough = item.user.type == UserType.Banned
        holder.timeView.text = elapsedTimeFormatter.format(item.time)
        holder.descriptionView.text = item.text

        glide.load(item.user.avatarUrl)
            .fallback(R.drawable.default_avatar_64dp)
            .circleCrop()
            .dontAnimate()
            .into(holder.avatarView)
    }

    class ViewHolder(itemView: View, private val onClickListener: OnViewHolderClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val avatarView: ImageView = itemView.findViewById(R.id.avatar)
        val usernameView: TextView = itemView.findViewById(R.id.username)
        val timeView: TextView = itemView.findViewById(R.id.time)
        val descriptionView: TextView = itemView.findViewById(R.id.description)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            onClickListener.onViewHolderClick(this, view)
        }
    }
}

private class FavoriteItemDelegate(
    private val glide: GlideRequests,
    private val elapsedTimeFormatter: ElapsedTimeFormatter,
    private val onViewHolderClickListener: OnViewHolderClickListener
) : BaseAdapterDelegate<FavoriteItem, ListItem, FavoriteItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is FavoriteItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_notifications_simple)
        return ViewHolder(view, onViewHolderClickListener)
    }

    override fun onBindViewHolder(item: FavoriteItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.usernameView.text = item.user.name
        holder.usernameView.isStrikethrough = item.user.type == UserType.Banned
        holder.timeView.text = elapsedTimeFormatter.format(item.time)
        holder.descriptionView.text = HtmlCompat.fromHtml(holder.itemView.resources.getString(R.string.notifications_item_favorite, item.deviationTitle), 0)

        glide.load(item.user.avatarUrl)
            .fallback(R.drawable.default_avatar_64dp)
            .circleCrop()
            .dontAnimate()
            .into(holder.avatarView)
    }

    class ViewHolder(itemView: View, private val onClickListener: OnViewHolderClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val avatarView: ImageView = itemView.findViewById(R.id.avatar)
        val usernameView: TextView = itemView.findViewById(R.id.username)
        val timeView: TextView = itemView.findViewById(R.id.time)
        val descriptionView: TextView = itemView.findViewById(R.id.description)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            onClickListener.onViewHolderClick(this, view)
        }
    }
}

private class WatchItemDelegate(
    private val glide: GlideRequests,
    private val elapsedTimeFormatter: ElapsedTimeFormatter,
    private val onViewHolderClickListener: OnViewHolderClickListener
) : BaseAdapterDelegate<WatchItem, ListItem, WatchItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is WatchItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_notifications_simple)
        return ViewHolder(view, onViewHolderClickListener)
    }

    override fun onBindViewHolder(item: WatchItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.usernameView.text = item.user.name
        holder.usernameView.isStrikethrough = item.user.type == UserType.Banned
        holder.timeView.text = elapsedTimeFormatter.format(item.time)
        holder.descriptionView.text = holder.itemView.resources.getString(R.string.notifications_item_watch)

        glide.load(item.user.avatarUrl)
            .fallback(R.drawable.default_avatar_64dp)
            .circleCrop()
            .dontAnimate()
            .into(holder.avatarView)
    }

    class ViewHolder(itemView: View, private val onClickListener: OnViewHolderClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val avatarView: ImageView = itemView.findViewById(R.id.avatar)
        val usernameView: TextView = itemView.findViewById(R.id.username)
        val timeView: TextView = itemView.findViewById(R.id.time)
        val descriptionView: TextView = itemView.findViewById(R.id.description)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            onClickListener.onViewHolderClick(this, view)
        }
    }
}

internal class NotificationsAdapter(
    glide: GlideRequests,
    elapsedTimeFormatter: ElapsedTimeFormatter,
    private val onOpenCollection: OnOpenCollectionListener,
    private val onOpenComment: OnOpenCommentListener,
    private val onOpenDeviation: OnOpenDeviationListener,
    private val onOpenStatus: OnOpenStatusListener,
    private val onOpenUserProfile: OnOpenUserProfileListener
) : ListDelegationAdapter<List<ListItem>>(), OnViewHolderClickListener {

    init {
        delegatesManager.addDelegate(LoadingIndicatorItemDelegate.VIEW_TYPE, LoadingIndicatorItemDelegate())
        delegatesManager.addDelegate(AddToCollectionItemDelegate(glide, elapsedTimeFormatter, this))
        delegatesManager.addDelegate(BadgeGivenItemDelegate(glide, elapsedTimeFormatter, this))
        delegatesManager.addDelegate(FavoriteItemDelegate(glide, elapsedTimeFormatter, this))
        delegatesManager.addDelegate(WatchItemDelegate(glide, elapsedTimeFormatter, this))
    }

    override fun onViewHolderClick(holder: RecyclerView.ViewHolder, view: View) {
        val position = holder.adapterPosition
        if (position == RecyclerView.NO_POSITION) return
        when (val item = items[position]) {
            is AddToCollectionItem -> onOpenCollection(item.user.name, item.folderId, item.folderName)
            is BadgeGivenItem -> onOpenUserProfile(item.user)
            is FavoriteItem -> onOpenUserProfile(item.user)
            is WatchItem -> onOpenUserProfile(item.user)
        }
    }
}

private typealias OnOpenCollectionListener = (username: String, folderId: String, folderName: String) -> Unit
private typealias OnOpenCommentListener = (commentId: String) -> Unit
private typealias OnOpenDeviationListener = (deviationId: String) -> Unit
private typealias OnOpenStatusListener = (statusId: String) -> Unit
private typealias OnOpenUserProfileListener = (user: User) -> Unit
