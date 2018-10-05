package io.plastique.core.lists

import androidx.recyclerview.widget.RecyclerView

abstract class BaseListAdapter<T, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {
    var items: List<T> = emptyList()

    final override fun getItemCount(): Int = items.size

    final override fun onBindViewHolder(holder: VH, position: Int) {
        onBindViewHolder(items[position], holder)
    }

    protected abstract fun onBindViewHolder(item: T, holder: VH)
}
