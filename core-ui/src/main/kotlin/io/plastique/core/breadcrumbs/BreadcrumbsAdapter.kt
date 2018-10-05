package io.plastique.core.breadcrumbs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.core.lists.BaseAdapterDelegate
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.OnViewHolderClickListener
import io.plastique.core.lists.calculateDiff

class BreadcrumbItemDelegate(
    private val layoutId: Int,
    private val onClickListener: OnViewHolderClickListener
) : BaseAdapterDelegate<BreadcrumbItem, ListItem, BreadcrumbItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is BreadcrumbItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ViewHolder(view, onClickListener)
    }

    override fun onBindViewHolder(item: BreadcrumbItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.textView.text = item.breadcrumb.text
    }

    class ViewHolder(
        itemView: View,
        private val onClickListener: OnViewHolderClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val textView: TextView = itemView.findViewById(android.R.id.text1)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            onClickListener.onViewHolderClick(this, view)
        }
    }
}

class SeparatorItemDelegate(
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

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

class BreadcrumbsAdapter(
    layoutId: Int,
    separatorDrawableResId: Int
) : ListDelegationAdapter<List<ListItem>>(), OnViewHolderClickListener {
    var onBreadcrumbClickListener: OnBreadcrumbClickListener? = null

    init {
        delegatesManager.addDelegate(BreadcrumbItemDelegate(layoutId, this))
        delegatesManager.addDelegate(SeparatorItemDelegate(separatorDrawableResId))
    }

    fun update(items: List<ListItem>) {
        val updateData = calculateDiff(this.items ?: emptyList(), items)
        updateData.applyTo(this)
    }

    override fun onViewHolderClick(holder: RecyclerView.ViewHolder, view: View) {
        val position = holder.adapterPosition
        if (position != RecyclerView.NO_POSITION) {
            val item = items[position] as BreadcrumbItem
            onBreadcrumbClickListener?.invoke(item.breadcrumb)
        }
    }
}
