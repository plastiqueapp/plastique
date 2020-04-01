package io.plastique.settings.licenses

import android.view.ViewGroup
import androidx.core.view.isVisible
import com.github.technoir42.android.extensions.layoutInflater
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.core.lists.BaseAdapterDelegate
import io.plastique.core.lists.ListItem
import io.plastique.settings.databinding.ItemLicensesHeaderBinding
import io.plastique.settings.databinding.ItemLicensesLicenseBinding

private class HeaderItemDelegate : BaseAdapterDelegate<HeaderItem, ListItem, HeaderItemDelegate.ViewHolder>() {
    override fun isForViewType(item: ListItem): Boolean = item === HeaderItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val binding = ItemLicensesHeaderBinding.inflate(parent.layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(item: HeaderItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
    }

    class ViewHolder(binding: ItemLicensesHeaderBinding) : BaseAdapterDelegate.ViewHolder<HeaderItem>(binding.root)
}

private class LicenseItemDelegate(
    private val onLicenseClick: OnLicenseClickListener
) : BaseAdapterDelegate<LicenseItem, ListItem, LicenseItemDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem): Boolean = item is LicenseItem

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val binding = ItemLicensesLicenseBinding.inflate(parent.layoutInflater, parent, false)
        return ViewHolder(binding, onLicenseClick)
    }

    override fun onBindViewHolder(item: LicenseItem, holder: ViewHolder, position: Int, payloads: List<Any>) {
        holder.binding.libraryName.text = item.license.libraryName
        holder.binding.libraryDescription.text = item.license.libraryDescription
        holder.binding.libraryDescription.isVisible = !item.license.libraryDescription.isNullOrEmpty()
        holder.binding.license.text = item.license.license
    }

    class ViewHolder(
        val binding: ItemLicensesLicenseBinding,
        onLicenseClick: OnLicenseClickListener
    ) : BaseAdapterDelegate.ViewHolder<LicenseItem>(binding.root) {

        init {
            binding.root.setOnClickListener { onLicenseClick(item.license) }
        }
    }
}

internal class LicensesAdapter(
    onLicenseClick: OnLicenseClickListener
) : ListDelegationAdapter<List<ListItem>>() {

    init {
        delegatesManager.addDelegate(TYPE_HEADER, HeaderItemDelegate())
        delegatesManager.addDelegate(TYPE_LICENSE, LicenseItemDelegate(onLicenseClick))
    }

    fun update(items: List<ListItem>) {
        this.items = items
        notifyDataSetChanged()
    }

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_LICENSE = 1
    }
}

internal typealias OnLicenseClickListener = (License) -> Unit
