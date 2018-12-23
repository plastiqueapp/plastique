package io.plastique.deviations.categories.list

import android.content.res.Resources
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ViewSwitcher
import androidx.recyclerview.widget.RecyclerView
import io.plastique.core.lists.BaseListAdapter
import io.plastique.core.lists.OnViewHolderClickListener
import io.plastique.deviations.R
import kotlin.math.max

class CategoriesAdapter(
    private val onItemClick: OnItemClickListener
) : BaseListAdapter<CategoryItem, CategoriesAdapter.ViewHolder>(), OnViewHolderClickListener {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return ViewHolder(view, this)
    }

    override fun onBindViewHolder(item: CategoryItem, holder: ViewHolder) {
        holder.textView.text = getItemText(holder.itemView.resources, item)
        holder.progressSwitcher.removeCallbacks(holder.showProgressCallback)
        if (!item.parent && item.category.hasChildren) {
            // Show progress bar after a small delay
            val remainingTime = max(0, PROGRESS_BAR_SHOW_DELAY - SystemClock.elapsedRealtime() + item.startLoadingTimestamp)
            holder.progressSwitcher.displayedChild = if (item.loading && remainingTime == 0L) 1 else 0
            holder.progressSwitcher.visibility = View.VISIBLE
            if (item.loading && remainingTime > 0) {
                holder.progressSwitcher.postDelayed(holder.showProgressCallback, remainingTime)
            }
        } else {
            holder.progressSwitcher.visibility = View.GONE
        }
    }

    override fun onViewHolderClick(holder: RecyclerView.ViewHolder, view: View) {
        val position = holder.adapterPosition
        if (position != RecyclerView.NO_POSITION) {
            onItemClick(items[position])
        }
    }

    private fun getItemText(resources: Resources, item: CategoryItem): String {
        return if (item.parent) {
            resources.getString(R.string.deviations_categories_see_all_in, item.category.title)
        } else {
            item.category.title
        }
    }

    class ViewHolder(
        itemView: View,
        private val listener: OnViewHolderClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val textView: TextView = itemView.findViewById(R.id.text)
        val progressSwitcher: ViewSwitcher = itemView.findViewById(R.id.progress_switcher)
        val showProgressCallback: Runnable = Runnable { progressSwitcher.displayedChild = 1 }

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            listener.onViewHolderClick(this, view)
        }
    }

    companion object {
        private const val PROGRESS_BAR_SHOW_DELAY = 200
    }
}

typealias OnItemClickListener = (CategoryItem) -> Unit
