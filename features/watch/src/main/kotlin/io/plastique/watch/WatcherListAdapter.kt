package io.plastique.watch

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.github.technoir42.android.extensions.inflate
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.core.image.ImageLoader
import io.plastique.core.image.TransformType
import io.plastique.core.lists.BaseAdapterDelegate
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItemDelegate
import io.plastique.users.OnUserClickListener

private class WatcherItemDelegate(
    private val imageLoader: ImageLoader,
    private val onUserClick: OnUserClickListener
) : BaseAdapterDelegate<WatcherItem, ListItem, WatcherItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is WatcherItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_watcher)
        return ViewHolder(view, onUserClick)
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
        onUserClick: OnUserClickListener
    ) : BaseAdapterDelegate.ViewHolder<WatcherItem>(itemView) {
        val avatar: ImageView = itemView.findViewById(R.id.avatar)
        val username: TextView = itemView.findViewById(R.id.username)

        init {
            itemView.setOnClickListener { onUserClick(item.watcher.user) }
        }
    }
}

internal class WatcherListAdapter(
    imageLoader: ImageLoader,
    onUserClick: OnUserClickListener
) : ListDelegationAdapter<List<ListItem>>() {

    init {
        delegatesManager.addDelegate(WatcherItemDelegate(imageLoader, onUserClick))
        delegatesManager.addDelegate(LoadingIndicatorItemDelegate())
    }
}
