package io.plastique.deviations.tags

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.Px
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import io.plastique.core.lists.BaseListAdapter
import io.plastique.core.lists.OnViewHolderClickListener

internal class TagsAdapter(
    private val tagBackgroundResId: Int,
    private val tagTextAppearance: Int,
    @Px private val tagMargin: Int
) : BaseListAdapter<Tag, TagsAdapter.ViewHolder>(), OnViewHolderClickListener {
    var onTagClickListener: OnTagClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutParams = (parent as RecyclerView).layoutManager!!.generateDefaultLayoutParams()
        layoutParams.setMargins(tagMargin, tagMargin, tagMargin, tagMargin)

        val view = AppCompatTextView(parent.context)
        view.setBackgroundResource(tagBackgroundResId)
        view.layoutParams = layoutParams
        TextViewCompat.setTextAppearance(view, tagTextAppearance)
        return ViewHolder(view, this)
    }

    override fun onBindViewHolder(item: Tag, holder: ViewHolder) {
        holder.textView.text = item.text
    }

    override fun onViewHolderClick(holder: RecyclerView.ViewHolder, view: View) {
        val position = holder.adapterPosition
        if (position != RecyclerView.NO_POSITION) {
            onTagClickListener?.onTagClick(items[position])
        }
    }

    class ViewHolder(
        val textView: TextView,
        private val listener: OnViewHolderClickListener
    ) : RecyclerView.ViewHolder(textView), View.OnClickListener {
        init {
            textView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            listener.onViewHolderClick(this, view)
        }
    }
}
