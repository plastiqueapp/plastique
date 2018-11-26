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
import io.plastique.glide.GlideApp

private class WatcherItemDelegate(private val onViewHolderClickListener: OnViewHolderClickListener) : BaseAdapterDelegate<WatcherItem, ListItem, WatcherItemDelegate.ViewHolder>() {
    override fun isForViewType(item: ListItem): Boolean = item is WatcherItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_watcher, parent, false)
        return ViewHolder(view, onViewHolderClickListener)
    }

    override fun onBindViewHolder(item: WatcherItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.username.text = item.watcher.username

        GlideApp.with(holder.avatar)
                .load(item.watcher.avatarUrl)
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

class WatcherListAdapter : ListDelegationAdapter<List<ListItem>>(), OnViewHolderClickListener {
    var onWatcherClickListener: OnWatcherClickListener? = null

    init {
        delegatesManager.addDelegate(WatcherItemDelegate(this))
        delegatesManager.addDelegate(LoadingIndicatorItemDelegate())
    }

    override fun onViewHolderClick(holder: RecyclerView.ViewHolder, view: View) {
        val position = holder.adapterPosition
        val item = if (position != RecyclerView.NO_POSITION) items[position] else return
        onWatcherClickListener?.invoke(item as WatcherItem)
    }
}

typealias OnWatcherClickListener = (WatcherItem) -> Unit
