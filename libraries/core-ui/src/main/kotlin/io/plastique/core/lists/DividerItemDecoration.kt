package io.plastique.core.lists

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

class DividerItemDecoration private constructor(
    private val divider: Drawable,
    private val viewTypes: IntArray?
) : RecyclerView.ItemDecoration() {
    private val rect = Rect()

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.layoutManager == null) {
            return
        }

        canvas.save()
        val left: Int
        val right: Int
        if (parent.clipToPadding) {
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
            canvas.clipRect(left, parent.paddingTop, right, parent.height - parent.paddingBottom)
        } else {
            left = 0
            right = parent.width
        }

        parent.children.forEach { child ->
            val holder = parent.getChildViewHolder(child)
            if (isDecorated(holder, parent)) {
                parent.getDecoratedBoundsWithMargins(child, rect)
                val bottom = rect.bottom + child.translationY.roundToInt()
                val top = bottom - divider.intrinsicHeight
                divider.setBounds(left, top, right, bottom)
                divider.draw(canvas)
            }
        }
        canvas.restore()
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val holder = parent.getChildViewHolder(view)
        if (isDecorated(holder, parent)) {
            outRect.set(0, 0, 0, divider.intrinsicHeight)
        } else {
            outRect.set(0, 0, 0, 0)
        }
    }

    private fun isDecorated(holder: RecyclerView.ViewHolder, parent: RecyclerView): Boolean {
        val position = holder.adapterPosition
        if (position == RecyclerView.NO_POSITION || position + 1 >= parent.layoutManager!!.itemCount) {
            return false
        }
        return viewTypes == null || viewTypes.any { it == holder.itemViewType }
    }

    class Builder(private val context: Context) {
        private var divider: Drawable? = null
        private var viewTypes: IntArray? = null

        fun divider(divider: Drawable): Builder {
            this.divider = divider
            return this
        }

        fun divider(@DrawableRes resId: Int): Builder {
            divider = context.getDrawable(resId)
            return this
        }

        fun viewTypes(vararg viewTypes: Int): Builder {
            this.viewTypes = viewTypes
            return this
        }

        fun build(): DividerItemDecoration {
            return DividerItemDecoration(
                divider = divider ?: getDefaultDivider(context),
                viewTypes = viewTypes)
        }

        private fun getDefaultDivider(context: Context): Drawable {
            val a = context.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
            val defaultDivider = a.getDrawable(0)!!
            a.recycle()
            return defaultDivider
        }
    }
}
