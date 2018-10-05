package io.plastique.core.lists

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class ListItemDiffTransformer<T : ListItem> : ObservableTransformer<List<T>, ListUpdateData<T>> {
    private val firstUpdate = AtomicBoolean(true)

    override fun apply(upstream: Observable<List<T>>): ObservableSource<ListUpdateData<T>> {
        return upstream.distinctUntilChanged()
                .scan(ListUpdateData.empty<T>()) { prev, items ->
                    if (firstUpdate.compareAndSet(true, false)) {
                        ListUpdateData(items, null)
                    } else {
                        calculateDiff(prev.items, items)
                    }
                }
                .skip(1)
    }
}

fun <T : ListItem> calculateDiff(oldItems: List<T>, newItems: List<T>): ListUpdateData<T> {
    val diffResult = DiffUtil.calculateDiff(ListDiffCallback(oldItems, newItems, ListItemCallback))
    return ListUpdateData(newItems, diffResult)
}

class ListUpdateData<T>(
    val items: List<T>,
    private val diffResult: DiffUtil.DiffResult?
) {

    fun applyTo(adapter: BaseListAdapter<T, *>) {
        if (adapter.items.isEmpty() || diffResult == null) {
            adapter.items = items
            adapter.notifyDataSetChanged()
        } else {
            adapter.items = items
            diffResult.dispatchUpdatesTo(adapter)
        }
    }

    fun applyTo(adapter: ListDelegationAdapter<List<T>>) {
        if (adapter.items == null || diffResult == null) {
            adapter.items = items
            adapter.notifyDataSetChanged()
        } else {
            adapter.items = items
            diffResult.dispatchUpdatesTo(adapter)
        }
    }

    fun log(tag: String) {
        if (diffResult != null) {
            diffResult.dispatchUpdatesTo(LoggingListUpdateCallback(tag))
        } else {
            Timber.tag(tag).d("notifyDataSetChanged")
        }
    }

    companion object {
        fun <T> empty(): ListUpdateData<T> {
            return ListUpdateData(emptyList(), null)
        }
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
