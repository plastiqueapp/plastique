package io.plastique.deviations.info

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.plastique.core.lists.BaseListAdapter
import io.plastique.core.lists.OnViewHolderClickListener
import io.plastique.deviations.R

class TagListAdapter(private val onTagClick: OnTagClickListener) : BaseListAdapter<String, TagListAdapter.ViewHolder>(), OnViewHolderClickListener {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_deviation_tag, parent, false)
        return ViewHolder(view, this)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(item: String, holder: ViewHolder) {
        holder.textView.text = "#$item"
    }

    override fun onViewHolderClick(holder: RecyclerView.ViewHolder, view: View) {
        val tag = items[holder.adapterPosition]
        onTagClick(tag)
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

typealias OnTagClickListener = (tag: String) -> Unit
