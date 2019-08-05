package io.plastique.feed.settings

import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.github.technoir42.android.extensions.inflate
import com.google.android.material.switchmaterial.SwitchMaterial
import io.plastique.core.lists.BaseListAdapter
import io.plastique.feed.R

internal class OptionsAdapter : BaseListAdapter<OptionItem, OptionsAdapter.ViewHolder>(), CompoundButton.OnCheckedChangeListener {
    var onOptionCheckedChangedListener: OnOptionCheckedChangedListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = parent.inflate(R.layout.item_feed_settings_option)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(item: OptionItem, holder: ViewHolder, position: Int) {
        holder.optionView.text = item.title
        holder.optionView.tag = item.key
        holder.optionView.setOnCheckedChangeListener(null)
        holder.optionView.isChecked = item.isChecked
        holder.optionView.setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        onOptionCheckedChangedListener?.invoke(buttonView.tag as String, isChecked)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val optionView: SwitchMaterial = itemView.findViewById(R.id.option)
    }
}

typealias OnOptionCheckedChangedListener = (key: String, checked: Boolean) -> Unit
