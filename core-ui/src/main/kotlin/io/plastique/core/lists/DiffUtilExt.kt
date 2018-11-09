package io.plastique.core.lists

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import timber.log.Timber
import java.util.concurrent.atomic.AtomicReference

class ListItemDiffTransformer<T : ListItem> : ObservableTransformer<List<T>, ListUpdateData<T>> {
    override fun apply(upstream: Observable<List<T>>): ObservableSource<ListUpdateData<T>> {
        val previous = AtomicReference<List<T>>()
        return upstream
                .map { items -> calculateDiff(previous.getAndSet(items), items) }
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : ListItem> calculateDiff(oldItems: List<T>?, newItems: List<T>): ListUpdateData<T> = when {
    oldItems.orEmpty() == newItems -> ListUpdateData.Empty as ListUpdateData<T>
    oldItems.isNullOrEmpty() -> ListUpdateData.Full(newItems)
    else -> {
        val diffResult = DiffUtil.calculateDiff(ListDiffCallback(oldItems, newItems, ListItemCallback))
        ListUpdateData.Diff(newItems, diffResult)
    }
}

sealed class ListUpdateData<T> {
    abstract val items: List<T>

    abstract fun applyTo(adapter: BaseListAdapter<T, *>)

    abstract fun applyTo(adapter: ListDelegationAdapter<List<T>>)

    abstract fun log(tag: String)

    data class Full<T : ListItem>(override val items: List<T>) : ListUpdateData<T>() {
        override fun applyTo(adapter: BaseListAdapter<T, *>) {
            adapter.items = items
            adapter.notifyDataSetChanged()
        }

        override fun applyTo(adapter: ListDelegationAdapter<List<T>>) {
            adapter.items = items
            adapter.notifyDataSetChanged()
        }

        override fun log(tag: String) {
            Timber.tag(tag).d("notifyDataSetChanged")
        }
    }

    data class Diff<T : ListItem>(override val items: List<T>, private val diffResult: DiffUtil.DiffResult) : ListUpdateData<T>() {
        override fun applyTo(adapter: BaseListAdapter<T, *>) {
            adapter.items = items
            diffResult.dispatchUpdatesTo(adapter)
        }

        override fun applyTo(adapter: ListDelegationAdapter<List<T>>) {
            adapter.items = items
            diffResult.dispatchUpdatesTo(adapter)
        }

        override fun log(tag: String) {
            diffResult.dispatchUpdatesTo(LoggingListUpdateCallback(tag))
        }
    }

    object Empty : ListUpdateData<Any>() {
        override val items: List<Any> get() = emptyList()

        override fun applyTo(adapter: BaseListAdapter<Any, *>) {
        }

        override fun applyTo(adapter: ListDelegationAdapter<List<Any>>) {
        }

        override fun log(tag: String) {
        }

        override fun toString(): String = "Empty"
    }
}

class ListDiffCallback<T>(
    private val oldItems: List<T>,
    private val newItems: List<T>,
    private val itemCallback: DiffUtil.ItemCallback<T>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldItems.size

    override fun getNewListSize(): Int = newItems.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            itemCallback.areItemsTheSame(oldItems[oldItemPosition], newItems[newItemPosition])

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            itemCallback.areContentsTheSame(oldItems[oldItemPosition], newItems[newItemPosition])

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? =
            itemCallback.getChangePayload(oldItems[oldItemPosition], newItems[newItemPosition])
}

private object ListItemCallback : DiffUtil.ItemCallback<ListItem>() {
    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean =
            oldItem.javaClass === newItem.javaClass && oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean =
            oldItem == newItem
}

private class LoggingListUpdateCallback(private val tag: String) : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {
        Timber.tag(tag).d("onInserted(position: %d, count: %d)", position, count)
    }

    override fun onRemoved(position: Int, count: Int) {
        Timber.tag(tag).d("onRemoved(position: %d, count: %d)", position, count)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        Timber.tag(tag).d("onMoved(fromPosition: %d, toPosition: %d)", fromPosition, toPosition)
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        Timber.tag(tag).d("onChanged(position: %d, count: %d, payload: %s)", position, count, payload)
    }
}
