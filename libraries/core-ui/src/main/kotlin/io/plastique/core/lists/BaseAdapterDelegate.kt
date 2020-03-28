package io.plastique.core.lists

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate

abstract class BaseAdapterDelegate<I : T, T : Any, VH : BaseAdapterDelegate.ViewHolder<I>> : AdapterDelegate<List<T>>() {
    final override fun isForViewType(items: List<T>, position: Int): Boolean = isForViewType(items[position])

    @Suppress("UNCHECKED_CAST")
    final override fun onBindViewHolder(items: List<T>, position: Int, holder: RecyclerView.ViewHolder, payloads: List<Any>) {
        val item = items[position] as I
        (holder as VH).item = item
        onBindViewHolder(item, holder, position, payloads)
    }

    protected abstract fun isForViewType(item: T): Boolean

    abstract override fun onCreateViewHolder(parent: ViewGroup): VH

    protected abstract fun onBindViewHolder(item: I, holder: VH, position: Int, payloads: List<Any>)

    abstract class ViewHolder<I : Any>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var item: I
    }
}
