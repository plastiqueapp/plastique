package io.plastique.core.breadcrumbs

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.github.technoir42.android.extensions.inflate
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.core.lists.BaseAdapterDelegate
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.calculateDiff

private class BreadcrumbItemDelegate(
    private val layoutId: Int,
    private val onBreadcrumbClick: OnBreadcrumbClickListener
) : BaseAdapterDelegate<BreadcrumbItem, ListItem, BreadcrumbItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is BreadcrumbItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(layoutId)
        return ViewHolder(view, onBreadcrumbClick)
    }

    override fun onBindViewHolder(item: BreadcrumbItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.textView.text = item.breadcrumb.text
    }

    class ViewHolder(
        itemView: View,
        onBreadcrumbClick: OnBreadcrumbClickListener
    ) : BaseAdapterDelegate.ViewHolder<BreadcrumbItem>(itemView) {
        val textView: TextView = itemView.findViewById(android.R.id.text1)

        init {
            itemView.setOnClickListener { onBreadcrumbClick(item.breadcrumb) }
        }
    }
}

private class SeparatorItemDelegate(
    private val separatorDrawableResId: Int
) : BaseAdapterDelegate<SeparatorItem, ListItem, SeparatorItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is SeparatorItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = AppCompatImageView(parent.context)
        view.setImageResource(separatorDrawableResId)
        view.scaleType = ImageView.ScaleType.CENTER
        view.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(item: SeparatorItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
    }

    class ViewHolder(itemView: View) : BaseAdapterDelegate.ViewHolder<SeparatorItem>(itemView)
}

internal class BreadcrumbsAdapter(
    layoutId: Int,
    separatorDrawableResId: Int,
    onBreadcrumbClick: OnBreadcrumbClickListener
) : ListDelegationAdapter<List<ListItem>>() {

    init {
        delegatesManager.addDelegate(BreadcrumbItemDelegate(layoutId, onBreadcrumbClick))
        delegatesManager.addDelegate(SeparatorItemDelegate(separatorDrawableResId))
    }

    fun update(items: List<ListItem>) {
        val updateData = calculateDiff(this.items, items)
        updateData.applyTo(this)
    }
}
