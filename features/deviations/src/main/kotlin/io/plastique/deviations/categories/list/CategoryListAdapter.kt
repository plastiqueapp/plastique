package io.plastique.deviations.categories.list

import android.content.res.Resources
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import com.github.technoir42.android.extensions.layoutInflater
import io.plastique.core.lists.BaseListAdapter
import io.plastique.deviations.R
import io.plastique.deviations.databinding.ItemCategoryBinding
import kotlin.math.max

internal class CategoryListAdapter(
    private val onCategoryClick: OnCategoryClickListener
) : BaseListAdapter<CategoryItem, CategoryListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(parent.layoutInflater, parent, false)
        return ViewHolder(binding, onCategoryClick)
    }

    override fun onBindViewHolder(item: CategoryItem, holder: ViewHolder, position: Int) {
        holder.binding.title.text = getItemText(holder.itemView.resources, item)
        holder.binding.progressSwitcher.removeCallbacks(holder.showProgressCallback)
        if (!item.isParent && item.category.hasChildren) {
            // Show progress bar after a small delay
            val remainingTime = max(0, PROGRESS_BAR_SHOW_DELAY - SystemClock.elapsedRealtime() + item.startLoadingTimestamp)
            holder.binding.progressSwitcher.displayedChild = if (item.isLoading && remainingTime == 0L) 1 else 0
            holder.binding.progressSwitcher.visibility = View.VISIBLE
            if (item.isLoading && remainingTime > 0) {
                holder.binding.progressSwitcher.postDelayed(holder.showProgressCallback, remainingTime)
            }
        } else {
            holder.binding.progressSwitcher.visibility = View.GONE
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
        val binding: ItemCategoryBinding,
        onCategoryClick: OnCategoryClickListener
    ) : BaseListAdapter.ViewHolder<CategoryItem>(binding.root) {
        val showProgressCallback: Runnable = Runnable { binding.progressSwitcher.displayedChild = 1 }

        init {
            binding.root.setOnClickListener { onCategoryClick(item) }
        }
    }

    companion object {
        private const val PROGRESS_BAR_SHOW_DELAY = 200
    }
}

private typealias OnCategoryClickListener = (CategoryItem) -> Unit
