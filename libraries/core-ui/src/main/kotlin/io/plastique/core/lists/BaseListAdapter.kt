package io.plastique.core.lists

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseListAdapter<T : Any, VH : BaseListAdapter.ViewHolder<T>> : RecyclerView.Adapter<VH>() {
    var items: List<T> = emptyList()

    final override fun getItemCount(): Int = items.size

    final override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.item = item
        onBindViewHolder(item, holder, position)
    }

    protected abstract fun onBindViewHolder(item: T, holder: VH, position: Int)

    abstract class ViewHolder<T : Any>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var item: T
    }
}
