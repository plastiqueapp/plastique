package io.plastique.watch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.core.lists.BaseAdapterDelegate
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.LoadingIndicatorItemDelegate
import io.plastique.core.lists.OnViewHolderClickListener
import io.plastique.glide.GlideRequests
import io.plastique.users.User

private class WatcherItemDelegate(
    private val glide: GlideRequests,
    private val onViewHolderClickListener: OnViewHolderClickListener
) : BaseAdapterDelegate<WatcherItem, ListItem, WatcherItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is WatcherItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_watcher, parent, false)
        return ViewHolder(view, onViewHolderClickListener)
    }

    override fun onBindViewHolder(item: WatcherItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.username.text = item.watcher.user.name

        glide.load(item.watcher.user.avatarUrl)
            .fallback(R.drawable.default_avatar_64dp)
            .circleCrop()
            .dontAnimate()
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

class WatcherListAdapter(
    glide: GlideRequests,
    private val onUserClick: OnUserClickListener
) : ListDelegationAdapter<List<ListItem>>(), OnViewHolderClickListener {

    init {
        delegatesManager.addDelegate(WatcherItemDelegate(glide, this))
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

typealias OnUserClickListener = (user: User) -> Unit
