package io.plastique.deviations.tags

import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.Px
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import io.plastique.core.lists.BaseListAdapter

internal class TagsAdapter(
    private val tagBackgroundResId: Int,
    private val tagTextAppearance: Int,
    @Px private val tagMargin: Int,
    private val onTagClick: OnTagClickListener
) : BaseListAdapter<Tag, TagsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutParams = (parent as RecyclerView).layoutManager!!.generateDefaultLayoutParams()
        layoutParams.setMargins(tagMargin, tagMargin, tagMargin, tagMargin)

        val view = AppCompatTextView(parent.context)
        view.setBackgroundResource(tagBackgroundResId)
        view.layoutParams = layoutParams
        TextViewCompat.setTextAppearance(view, tagTextAppearance)
        return ViewHolder(view, onTagClick)
    }

    override fun onBindViewHolder(item: Tag, holder: ViewHolder, position: Int) {
        holder.textView.text = item.text
    }

    class ViewHolder(
        val textView: TextView,
        onTagClick: OnTagClickListener
    ) : BaseListAdapter.ViewHolder<Tag>(textView) {
        init {
            textView.setOnClickListener { onTagClick(item) }
        }
    }
}
