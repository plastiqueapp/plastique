package io.plastique.deviations.categories.list

import android.content.res.Resources
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ViewSwitcher
import com.github.technoir42.android.extensions.inflate
import io.plastique.core.lists.BaseListAdapter
import io.plastique.deviations.R
import kotlin.math.max

internal class CategoryListAdapter(
    private val onCategoryClick: OnCategoryClickListener
) : BaseListAdapter<CategoryItem, CategoryListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = parent.inflate(R.layout.item_category)
        return ViewHolder(view, onCategoryClick)
    }

    override fun onBindViewHolder(item: CategoryItem, holder: ViewHolder, position: Int) {
        holder.textView.text = getItemText(holder.itemView.resources, item)
        holder.progressSwitcher.removeCallbacks(holder.showProgressCallback)
        if (!item.isParent && item.category.hasChildren) {
            // Show progress bar after a small delay
            val remainingTime = max(0, PROGRESS_BAR_SHOW_DELAY - SystemClock.elapsedRealtime() + item.startLoadingTimestamp)
            holder.progressSwitcher.displayedChild = if (item.isLoading && remainingTime == 0L) 1 else 0
            holder.progressSwitcher.visibility = View.VISIBLE
            if (item.isLoading && remainingTime > 0) {
                holder.progressSwitcher.postDelayed(holder.showProgressCallback, remainingTime)
            }
        } else {
            holder.progressSwitcher.visibility = View.GONE
        }
    }

    private fun getItemText(resources: Resources, item: CategoryItem): String {
        return if (item.isParent) {
            resources.getString(R.string.deviations_categories_see_all_in, item.category.title)
        } else {
            item.category.title
        }
    }

    class ViewHolder(
        itemView: View,
        onCategoryClick: OnCategoryClickListener
    ) : BaseListAdapter.ViewHolder<CategoryItem>(itemView) {
        val textView: TextView = itemView.findViewById(R.id.text)
        val progressSwitcher: ViewSwitcher = itemView.findViewById(R.id.progress_switcher)
        val showProgressCallback: Runnable = Runnable { progressSwitcher.displayedChild = 1 }

        init {
            itemView.setOnClickListener { onCategoryClick(item) }
        }
    }

    companion object {
        private const val PROGRESS_BAR_SHOW_DELAY = 200
    }
}

private typealias OnCategoryClickListener = (CategoryItem) -> Unit
