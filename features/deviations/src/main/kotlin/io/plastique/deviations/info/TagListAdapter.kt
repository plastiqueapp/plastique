package io.plastique.deviations.info

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.technoir42.android.extensions.inflate
import io.plastique.core.lists.BaseListAdapter
import io.plastique.deviations.R

internal class TagListAdapter(
    private val onTagClick: OnTagClickListener
) : BaseListAdapter<String, TagListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = parent.inflate(R.layout.item_deviation_tag)
        return ViewHolder(view, onTagClick)
    }

    override fun onBindViewHolder(item: String, holder: ViewHolder, position: Int) {
        holder.textView.text = holder.textView.resources.getString(R.string.common_hashtag, item)

        (holder.textView.layoutParams as ViewGroup.MarginLayoutParams).apply {
            leftMargin = if (position != 0) holder.itemView.resources.getDimensionPixelOffset(R.dimen.deviations_info_tag_spacing) else 0
        }
    }

    class ViewHolder(
        itemView: View,
        onTagClick: OnTagClickListener
    ) : BaseListAdapter.ViewHolder<String>(itemView) {
        val textView: TextView = itemView.findViewById(android.R.id.text1)

        init {
            itemView.setOnClickListener { onTagClick(item) }
        }
    }
}

private typealias OnTagClickListener = (tag: String) -> Unit
