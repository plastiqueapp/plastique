package io.plastique.core.lists

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.plastique.core.ui.R

class LoadingIndicatorItemDelegate : BaseAdapterDelegate<LoadingIndicatorItem, ListItem, LoadingIndicatorItemDelegate.ViewHolder>() {
    override fun isForViewType(item: ListItem): Boolean = item === LoadingIndicatorItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(item: LoadingIndicatorItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        const val VIEW_TYPE = 0
    }
}
