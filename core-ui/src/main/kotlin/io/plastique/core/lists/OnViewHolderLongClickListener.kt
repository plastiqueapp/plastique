package io.plastique.core.lists

import android.view.View
import androidx.recyclerview.widget.RecyclerView

interface OnViewHolderLongClickListener {
    fun onViewHolderLongClick(holder: RecyclerView.ViewHolder, view: View): Boolean
}
