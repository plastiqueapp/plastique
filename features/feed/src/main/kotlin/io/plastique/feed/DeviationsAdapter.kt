package io.plastique.feed

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import io.plastique.core.lists.ItemSizeCallback
import io.plastique.core.lists.ListItem
import io.plastique.core.lists.OnViewHolderClickListener
import io.plastique.deviations.list.DeviationItem
import io.plastique.deviations.list.GridImageDeviationItemDelegate
import io.plastique.deviations.list.GridLiteratureDeviationItemDelegate
import io.plastique.deviations.list.GridVideoDeviationItemDelegate
import io.plastique.deviations.list.LayoutMode
import io.plastique.glide.GlideRequests

internal class DeviationsAdapter(
    glide: GlideRequests,
    itemSizeCallback: ItemSizeCallback,
    private val onDeviationClick: OnDeviationClickListener
) : ListDelegationAdapter<List<ListItem>>(), OnViewHolderClickListener {

    init {
        val layoutModeProvider = { LayoutMode.Grid }
        delegatesManager.addDelegate(GridImageDeviationItemDelegate(glide, layoutModeProvider, itemSizeCallback, this))
        delegatesManager.addDelegate(GridLiteratureDeviationItemDelegate(layoutModeProvider, itemSizeCallback, this))
        delegatesManager.addDelegate(GridVideoDeviationItemDelegate(glide, layoutModeProvider, itemSizeCallback, this))
    }

    fun update(items: List<ListItem>) {
        if (this.items != items) {
            this.items = items
            notifyDataSetChanged()
        }
    }

    override fun onViewHolderClick(holder: RecyclerView.ViewHolder, view: View) {
        val position = holder.adapterPosition
        if (position != RecyclerView.NO_POSITION) {
            val item = items[position] as DeviationItem
            onDeviationClick(item.deviationId)
        }
    }
}
