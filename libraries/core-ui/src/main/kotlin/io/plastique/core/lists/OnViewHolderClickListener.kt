package io.plastique.core.lists

import android.view.View
import androidx.recyclerview.widget.RecyclerView

interface OnViewHolderClickListener {
    fun onViewHolderClick(holder: RecyclerView.ViewHolder, view: View)
}
