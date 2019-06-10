package io.plastique.core

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.customview.view.AbsSavedState
import com.github.technoir42.android.extensions.getLayoutBehavior
import com.github.technoir42.android.extensions.invalidateScrollRanges
import com.google.android.material.appbar.AppBarLayout
import io.plastique.core.ui.R
import io.plastique.util.OffsetLimitingBehavior
import timber.log.Timber
import kotlin.math.max

class ExpandableToolbarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    private var parent: AppBarLayout? = null
    private var offsetAnimator: ValueAnimator? = null
    private var pinnedHeight: Int = 0
    private var expandableHeight: Int = 0
    private var childrenOffset: Int = 0
    private var expanded: Boolean = false

    var isExpanded: Boolean
        get() = expanded
        set(value) = setExpanded(value, true)

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ExpandableToolbarLayout, defStyleAttr, defStyleRes)
        expanded = a.getBoolean(R.styleable.ExpandableToolbarLayout_expanded, false)
        a.recycle()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        parent = getParent() as AppBarLayout
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        parent = null
    }

    @Suppress("ComplexMethod")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Timber.d("onMeasure(widthMeasureSpec: %s, heightMeasureSpec: %s)", MeasureSpec.toString(widthMeasureSpec), MeasureSpec.toString(heightMeasureSpec))

        var totalHeight = 0
        var maxWidth = 0
        var childState = 0
        var alternativeMaxWidth = 0
        var allMatchParent = true

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var matchWidth = false
        pinnedHeight = 0
        expandableHeight = 0
        var afterPinnedView = false

        children.forEach { child ->
            if (child.visibility == View.GONE) {
                return@forEach
            }

            val lp = child.layoutParams as LayoutParams
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, totalHeight)

            val childHeightWithMargins = child.measuredHeight + lp.topMargin + lp.bottomMargin

            if (lp.pin) {
                pinnedHeight += childHeightWithMargins
            } else if (afterPinnedView) {
                expandableHeight += childHeightWithMargins
            }

            val totalLength = totalHeight
            totalHeight = max(totalLength, totalLength + childHeightWithMargins)

            var matchWidthLocally = false
            if (widthMode != MeasureSpec.EXACTLY && lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                // The width of the layout will scale, and at least one
                // child said it wanted to match our width. Set a flag
                // indicating that we need to remeasure at least that view when
                // we know our width.
                matchWidth = true
                matchWidthLocally = true
            }

            val margin = lp.leftMargin + lp.rightMargin
            val measuredWidth = child.measuredWidth + margin
            maxWidth = max(maxWidth, measuredWidth)
            childState = combineMeasuredStates(childState, child.measuredState)

            allMatchParent = allMatchParent && lp.width == ViewGroup.LayoutParams.MATCH_PARENT
            alternativeMaxWidth = max(alternativeMaxWidth, if (matchWidthLocally) margin else measuredWidth)

            if (lp.pin) {
                afterPinnedView = true
            }
        }

        totalHeight += paddingTop + paddingBottom

        var heightSize = totalHeight
        heightSize = max(heightSize, suggestedMinimumHeight)

        // Reconcile our calculated size with the heightMeasureSpec
        val heightSizeAndState = resolveSizeAndState(heightSize, heightMeasureSpec, 0)
        // Either expand children with weight to take up available space or
        // shrink them if they extend beyond our current bounds. If we skipped
        // measurement on any children, we need to measure them now.
        alternativeMaxWidth = max(alternativeMaxWidth, 0)

        if (!allMatchParent && widthMode != MeasureSpec.EXACTLY) {
            maxWidth = alternativeMaxWidth
        }

        maxWidth += paddingLeft + paddingRight
        maxWidth = max(maxWidth, suggestedMinimumWidth)

        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState), heightSizeAndState)

        if (matchWidth) {
            forceUniformWidth(heightMeasureSpec)
        }

        val maxAppBarOffset = if (expanded) 0 else -expandableHeight
        childrenOffset = -maxAppBarOffset

        val behavior = parent!!.getLayoutBehavior<OffsetLimitingBehavior>()
        behavior.maxOffset = maxAppBarOffset
        behavior.topAndBottomOffset = maxAppBarOffset

        Timber.d("maxAppBarOffset: %d", maxAppBarOffset)
        Timber.d("childrenOffset: %d", childrenOffset)
        Timber.d("minHeight: %d", minimumHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        Timber.d("onLayout(changed: %s, l: %d, t: %d, r: %d, b: %d)", changed, l, t, r, b)

        var childTop = childrenOffset + paddingTop

        children.forEach { child ->
            if (child.visibility == View.GONE) {
                return@forEach
            }

            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight

            val lp = child.layoutParams as LayoutParams
            val childLeft = paddingLeft + lp.leftMargin
            childTop += lp.topMargin
            child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight)
            childTop += childHeight + lp.bottomMargin
        }
    }

    override fun getMinimumHeight(): Int {
        return pinnedHeight + if (expanded) expandableHeight else 0
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean = p is LayoutParams

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams = LayoutParams(context, attrs)

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): LayoutParams = LayoutParams(p)

    override fun generateDefaultLayoutParams(): LayoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    override fun onSaveInstanceState(): Parcelable {
        val savedState = SavedState(super.onSaveInstanceState()!!)
        savedState.expanded = expanded
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        expanded = savedState.expanded
    }

    fun setExpanded(expanded: Boolean, animated: Boolean) {
        if (this.expanded == expanded) {
            return
        }
        this.expanded = expanded

        if (!animated || !isLaidOut) {
            requestLayout()
            return
        }

        val maxAppBarOffset = if (expanded) 0 else -expandableHeight
        val behavior = parent!!.getLayoutBehavior<OffsetLimitingBehavior>()
        behavior.maxOffset = maxAppBarOffset
        parent!!.invalidateScrollRanges()

        offsetAnimator?.cancel()

        val animator = offsetAnimator ?: ValueAnimator().apply {
            duration = EXPAND_ANIMATION_DURATION
            addUpdateListener { animation ->
                behavior.topAndBottomOffset = animation.getAnimatedValue("appBarOffset") as Int
                childrenOffset = animation.getAnimatedValue("childrenOffset") as Int
                offsetChildren(childrenOffset)
            }
        }

        animator.setValues(
            PropertyValuesHolder.ofInt("childrenOffset", childrenOffset, -maxAppBarOffset),
            PropertyValuesHolder.ofInt("appBarOffset", behavior.topAndBottomOffset, maxAppBarOffset))
        animator.start()

        offsetAnimator = animator
    }

    private fun offsetChildren(offset: Int) {
        var childTop = offset + paddingTop

        children.forEach { child ->
            if (child.visibility == View.GONE) {
                return@forEach
            }

            val lp = child.layoutParams as LayoutParams
            childTop += lp.topMargin
            val diff = childTop - child.top
            if (diff != 0) {
                ViewCompat.offsetTopAndBottom(child, diff)
            }
            childTop += child.measuredHeight + lp.bottomMargin
        }
    }

    private fun forceUniformWidth(heightMeasureSpec: Int) {
        // Pretend that layout has an exact size.
        val uniformMeasureSpec = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY)

        children.forEach { child ->
            if (child.visibility == View.GONE) {
                return@forEach
            }

            val lp = child.layoutParams as LayoutParams
            if (lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                // Temporarily force children to reuse their old measured height
                val oldHeight = lp.height
                lp.height = child.measuredHeight

                // Remeasure with new dimensions
                measureChildWithMargins(child, uniformMeasureSpec, 0, heightMeasureSpec, 0)
                lp.height = oldHeight
            }
        }
    }

    class LayoutParams : MarginLayoutParams {
        var pin: Boolean = false

        constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.ExpandableToolbarLayout_Layout)
            pin = a.getBoolean(R.styleable.ExpandableToolbarLayout_Layout_layout_pin, false)
            a.recycle()
        }

        constructor(width: Int, height: Int) : super(width, height)
        constructor(source: ViewGroup.LayoutParams) : super(source)
    }

    private class SavedState : AbsSavedState {
        var expanded: Boolean = false

        constructor(superState: Parcelable) : super(superState)

        constructor(source: Parcel, loader: ClassLoader?) : super(source, loader) {
            expanded = source.readInt() != 0
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(if (expanded) 1 else 0)
        }

        companion object CREATOR : Parcelable.ClassLoaderCreator<SavedState> {
            override fun createFromParcel(source: Parcel): SavedState = SavedState(source, null)

            override fun createFromParcel(source: Parcel, loader: ClassLoader?): SavedState = SavedState(source, loader)

            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }

    companion object {
        private const val EXPAND_ANIMATION_DURATION = 200L
    }
}
