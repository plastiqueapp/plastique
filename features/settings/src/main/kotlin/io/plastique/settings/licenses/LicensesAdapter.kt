package io.plastique.settings.licenses

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.github.technoir42.android.extensions.inflate
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.core.lists.BaseAdapterDelegate
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.OnViewHolderClickListener
import io.plastique.settings.R

private class HeaderItemDelegate : BaseAdapterDelegate<HeaderItem, ListItem, HeaderItemDelegate.ViewHolder>() {
    override fun isForViewType(item: ListItem): Boolean = item === HeaderItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_licenses_header)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(item: HeaderItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

private class LicenseItemDelegate(
    private val onViewHolderClickListener: OnViewHolderClickListener
) : BaseAdapterDelegate<LicenseItem, ListItem, LicenseItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is LicenseItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_licenses_license)
        return ViewHolder(view, onViewHolderClickListener)
    }

    override fun onBindViewHolder(item: LicenseItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.nameView.text = item.license.libraryName
        holder.descriptionView.text = item.license.libraryDescription
        holder.descriptionView.isVisible = !item.license.libraryDescription.isNullOrEmpty()
        holder.licenseView.text = item.license.license
    }

    class ViewHolder(
        itemView: View,
        private val onViewHolderClickListener: OnViewHolderClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val nameView: TextView = itemView.findViewById(R.id.library_name)
        val descriptionView: TextView = itemView.findViewById(R.id.library_description)
        val licenseView: TextView = itemView.findViewById(R.id.license)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            onViewHolderClickListener.onViewHolderClick(this, view)
        }
    }
}

internal class LicensesAdapter(
    private val onLicenseClick: OnLicenseClickListener
) : ListDelegationAdapter<List<ListItem>>(), OnViewHolderClickListener {

    init {
        delegatesManager.addDelegate(TYPE_HEADER, HeaderItemDelegate())
        delegatesManager.addDelegate(TYPE_LICENSE, LicenseItemDelegate(this))
    }

    fun update(items: List<ListItem>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onViewHolderClick(holder: RecyclerView.ViewHolder, view: View) {
        val position = holder.adapterPosition
        if (position != RecyclerView.NO_POSITION) {
            val item = items[position] as LicenseItem
            onLicenseClick(item.license)
        }
    }

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_LICENSE = 1
    }
}

private typealias OnLicenseClickListener = (License) -> Unit
