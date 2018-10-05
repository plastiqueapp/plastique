package io.plastique.core.lists

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate

abstract class BaseAdapterDelegate<I : T, T, VH : RecyclerView.ViewHolder> : AdapterDelegate<List<T>>() {
    final override fun isForViewType(items: List<T>, position: Int): Boolean = isForViewType(items[position])

    final override fun onBindViewHolder(items: List<T>, position: Int, holder: RecyclerView.ViewHolder, payloads: List<Any>) {
        @Suppress("UNCHECKED_CAST")
        onBindViewHolder(items[position] as I, holder as VH, position, payloads)
    }

    protected abstract fun isForViewType(item: T): Boolean

    abstract override fun onCreateViewHolder(parent: ViewGroup): VH

    protected abstract fun onBindViewHolder(item: I, holder: VH, position: Int, payloads: List<Any>)
}
