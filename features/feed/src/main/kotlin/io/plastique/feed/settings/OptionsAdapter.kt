package io.plastique.feed.settings

import android.view.ViewGroup
import android.widget.CompoundButton
import com.github.technoir42.android.extensions.layoutInflater
import io.plastique.core.lists.BaseListAdapter
import io.plastique.feed.databinding.ItemFeedSettingsOptionBinding

internal class OptionsAdapter(
    private val onOptionCheckedChanged: OnOptionCheckedChangedListener
) : BaseListAdapter<OptionItem, OptionsAdapter.ViewHolder>(), CompoundButton.OnCheckedChangeListener {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFeedSettingsOptionBinding.inflate(parent.layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(item: OptionItem, holder: ViewHolder, position: Int) {
        holder.binding.option.text = item.title
        holder.binding.option.tag = item.key
        holder.binding.option.setOnCheckedChangeListener(null)
        holder.binding.option.isChecked = item.isChecked
        holder.binding.option.setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        onOptionCheckedChanged(buttonView.tag as String, isChecked)
    }

    class ViewHolder(val binding: ItemFeedSettingsOptionBinding) : BaseListAdapter.ViewHolder<OptionItem>(binding.root)
}

private typealias OnOptionCheckedChangedListener = (key: String, checked: Boolean) -> Unit
