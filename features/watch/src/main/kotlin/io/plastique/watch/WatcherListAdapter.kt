package io.plastique.watch

import android.view.ViewGroup
import com.github.technoir42.android.extensions.layoutInflater
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.core.image.ImageLoader
import io.plastique.core.image.TransformType
import io.plastique.core.lists.BaseAdapterDelegate
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItemDelegate
import io.plastique.users.OnUserClickListener
import io.plastique.watch.databinding.ItemWatcherBinding

private class WatcherItemDelegate(
    private val imageLoader: ImageLoader,
    private val onUserClick: OnUserClickListener
) : BaseAdapterDelegate<WatcherItem, ListItem, WatcherItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is WatcherItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val binding = ItemWatcherBinding.inflate(parent.layoutInflater, parent, false)
        return ViewHolder(binding, onUserClick)
    }

    override fun onBindViewHolder(item: WatcherItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        val resources = holder.binding.root.resources
        holder.binding.avatar.contentDescription = resources.getString(R.string.common_avatar_description, item.watcher.user.name)
        holder.binding.username.text = item.watcher.user.name

        imageLoader.load(item.watcher.user.avatarUrl)
            .params {
                fallbackDrawable = R.drawable.default_avatar_64dp
                transforms += TransformType.CircleCrop
            }
            .into(holder.binding.avatar)
    }

    class ViewHolder(
        val binding: ItemWatcherBinding,
        onUserClick: OnUserClickListener
    ) : BaseAdapterDelegate.ViewHolder<WatcherItem>(binding.root) {

        init {
            binding.root.setOnClickListener { onUserClick(item.watcher.user) }
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
