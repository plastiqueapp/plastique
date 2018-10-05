package io.plastique.settings.about.licenses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.core.lists.BaseAdapterDelegate
import io.plastique.core.lists.OnViewHolderClickListener
import io.plastique.settings.R

class HeaderItemDelegate : BaseAdapterDelegate<HeaderItem, LicensesItem, HeaderItemDelegate.ViewHolder>() {
    override fun isForViewType(item: LicensesItem): Boolean = item === HeaderItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_licenses_header, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(item: HeaderItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

class LicenseItemDelegate(
    private val onViewHolderClickListener: OnViewHolderClickListener
) : BaseAdapterDelegate<LicenseItem, LicensesItem, LicenseItemDelegate.ViewHolder>() {

    override fun isForViewType(item: LicensesItem): Boolean = item is LicenseItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_licenses_license, parent, false)
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

class LicensesAdapter : ListDelegationAdapter<List<LicensesItem>>(), OnViewHolderClickListener {
    var onLicenseClickListener: OnLicenseClickListener? = null

    init {
        delegatesManager.addDelegate(TYPE_HEADER, HeaderItemDelegate())
        delegatesManager.addDelegate(TYPE_LICENSE, LicenseItemDelegate(this))
    }

    override fun onViewHolderClick(holder: RecyclerView.ViewHolder, view: View) {
        val position = holder.adapterPosition
        if (position != RecyclerView.NO_POSITION) {
            val item = items[position] as LicenseItem
            onLicenseClickListener?.invoke(item.license)
        }
    }

    fun update(items: List<LicensesItem>) {
        this.items = items
        notifyDataSetChanged()
    }

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_LICENSE = 1
    }
}

typealias OnLicenseClickListener = (License) -> Unit
