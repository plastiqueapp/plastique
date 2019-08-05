package io.plastique.deviations.info

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.technoir42.android.extensions.inflate
import io.plastique.core.lists.BaseListAdapter
import io.plastique.core.lists.OnViewHolderClickListener
import io.plastique.deviations.R

internal class TagListAdapter(
    private val onTagClick: OnTagClickListener
) : BaseListAdapter<String, TagListAdapter.ViewHolder>(), OnViewHolderClickListener {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = parent.inflate(R.layout.item_deviation_tag)
        return ViewHolder(view, this)
    }

    override fun onBindViewHolder(item: String, holder: ViewHolder, position: Int) {
        holder.textView.text = holder.textView.resources.getString(R.string.common_hashtag, item)

        (holder.textView.layoutParams as ViewGroup.MarginLayoutParams).apply {
            leftMargin = if (position != 0) holder.itemView.resources.getDimensionPixelOffset(R.dimen.deviations_info_tag_spacing) else 0
        }
    }

    override fun onViewHolderClick(holder: RecyclerView.ViewHolder, view: View) {
        val position = holder.adapterPosition
        if (position != RecyclerView.NO_POSITION) {
            onTagClick(items[position])
        }
    }

    class ViewHolder(itemView: View, private val onClickListener: OnViewHolderClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val textView: TextView = itemView.findViewById(android.R.id.text1)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            onClickListener.onViewHolderClick(this, view)
        }
    }
}

private typealias OnTagClickListener = (tag: String) -> Unit
