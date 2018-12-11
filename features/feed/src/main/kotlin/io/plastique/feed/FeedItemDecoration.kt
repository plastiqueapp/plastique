package io.plastique.feed

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView

class FeedItemDecoration(context: Context) : RecyclerView.ItemDecoration() {
    private val itemSpacing = context.resources.getDimensionPixelOffset(R.dimen.feed_element_spacing)
    private val dividerThickness = context.resources.getDimensionPixelSize(R.dimen.common_divider_thickness)
    private val dividerPaint = Paint().apply { color = ContextCompat.getColor(context, R.color.feed_divider) }
    private val bounds = Rect()

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.layoutManager == null) return

        parent.children.forEach { child ->
            val position = parent.getChildAdapterPosition(child)
            if (position > 0) {
                parent.getDecoratedBoundsWithMargins(child, bounds)
                bounds.top = bounds.top + itemSpacing / 2
                bounds.bottom = bounds.top + dividerThickness
                canvas.drawRect(bounds, dividerPaint)
            }
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val topOffset = if (position > 0) itemSpacing else 0
        outRect.set(0, topOffset, 0, 0)
    }
}
