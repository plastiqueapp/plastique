package io.plastique.watch

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.technoir42.android.extensions.inflate
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.core.image.ImageLoader
import io.plastique.core.image.TransformType
import io.plastique.core.lists.BaseAdapterDelegate
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItemDelegate
import io.plastique.core.lists.OnViewHolderClickListener
import io.plastique.users.User

private class WatcherItemDelegate(
    private val imageLoader: ImageLoader,
    private val onViewHolderClickListener: OnViewHolderClickListener
) : BaseAdapterDelegate<WatcherItem, ListItem, WatcherItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is WatcherItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_watcher)
        return ViewHolder(view, onViewHolderClickListener)
    }

    override fun onBindViewHolder(item: WatcherItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.avatar.contentDescription =
            holder.itemView.resources.getString(R.string.common_avatar_description, item.watcher.user.name)
        holder.username.text = item.watcher.user.name

        imageLoader.load(item.watcher.user.avatarUrl)
            .params {
                fallbackDrawable = R.drawable.default_avatar_64dp
                transforms += TransformType.CircleCrop
            }
            .into(holder.avatar)
    }

    class ViewHolder(
        itemView: View,
        private val onViewHolderClickListener: OnViewHolderClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val avatar: ImageView = itemView.findViewById(R.id.avatar)
        val username: TextView = itemView.findViewById(R.id.username)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            onViewHolderClickListener.onViewHolderClick(this, view)
        }
    }
}

internal class WatcherListAdapter(
    imageLoader: ImageLoader,
    private val onUserClick: OnUserClickListener
) : ListDelegationAdapter<List<ListItem>>(), OnViewHolderClickListener {

    init {
        delegatesManager.addDelegate(WatcherItemDelegate(imageLoader, this))
        delegatesManager.addDelegate(LoadingIndicatorItemDelegate())
    }

    override fun onViewHolderClick(holder: RecyclerView.ViewHolder, view: View) {
        val position = holder.adapterPosition
        if (position != RecyclerView.NO_POSITION) {
            val item = items[position] as WatcherItem
            onUserClick(item.watcher.user)
        }
    }
}

private typealias OnUserClickListener = (user: User) -> Unit
