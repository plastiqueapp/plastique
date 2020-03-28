package io.plastique.core.lists

import android.view.View
import android.view.ViewGroup
import com.github.technoir42.android.extensions.inflate
import io.plastique.core.ui.R

class LoadingIndicatorItemDelegate : BaseAdapterDelegate<LoadingIndicatorItem, ListItem, LoadingIndicatorItemDelegate.ViewHolder>() {
    override fun isForViewType(item: ListItem): Boolean = item === LoadingIndicatorItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_loading)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(item: LoadingIndicatorItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
    }

    class ViewHolder(itemView: View) : BaseAdapterDelegate.ViewHolder<LoadingIndicatorItem>(itemView)

    companion object {
        const val VIEW_TYPE = 0
    }
}
