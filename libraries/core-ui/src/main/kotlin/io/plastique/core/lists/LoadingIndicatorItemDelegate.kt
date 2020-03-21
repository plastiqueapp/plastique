package io.plastique.core.lists

import android.view.ViewGroup
import com.github.technoir42.android.extensions.layoutInflater
import io.plastique.core.ui.databinding.ItemLoadingBinding

class LoadingIndicatorItemDelegate : BaseAdapterDelegate<LoadingIndicatorItem, ListItem, LoadingIndicatorItemDelegate.ViewHolder>() {
    override fun isForViewType(item: ListItem): Boolean = item === LoadingIndicatorItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val binding = ItemLoadingBinding.inflate(parent.layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(item: LoadingIndicatorItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
    }

    class ViewHolder(binding: ItemLoadingBinding) : BaseAdapterDelegate.ViewHolder<LoadingIndicatorItem>(binding.root)

    companion object {
        const val VIEW_TYPE = 0
    }
}
