package io.plastique.core.breadcrumbs

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.customview.view.AbsSavedState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.plastique.core.extensions.getParcelableCreator
import io.plastique.core.lists.ListItem
import io.plastique.core.ui.R

internal data class BreadcrumbItem(override val id: String, val breadcrumb: Breadcrumb) : ListItem
internal data class SeparatorItem(override val id: String) : ListItem

class BreadcrumbsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = R.attr.breadcrumbsStyle
) : RecyclerView(context, attrs, defStyleAttr) {

    private val adapter: BreadcrumbsAdapter
    private var breadcrumbs: List<Breadcrumb> = emptyList()

    init {
        overScrollMode = View.OVER_SCROLL_NEVER

        val a = context.obtainStyledAttributes(attrs, R.styleable.BreadcrumbsView, defStyleAttr, 0)
        val breadcrumbLayoutId = a.getResourceId(R.styleable.BreadcrumbsView_breadcrumbLayoutId, 0)
        val separatorDrawableResId = a.getResourceId(R.styleable.BreadcrumbsView_separatorDrawable, 0)
        a.recycle()

        if (breadcrumbLayoutId == 0) {
            throw IllegalStateException("Required attribute breadcrumbLayoutId is not set")
        }

        adapter = BreadcrumbsAdapter(breadcrumbLayoutId, separatorDrawableResId)
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        setAdapter(adapter)
    }

    fun setBreadcrumbs(breadcrumbs: List<Breadcrumb>) {
        this.breadcrumbs = breadcrumbs.toList()
        adapter.update(createItems(breadcrumbs))
    }

    fun setOnBreadcrumbClickListener(listener: OnBreadcrumbClickListener) {
        adapter.onBreadcrumbClickListener = listener
    }

    override fun onSaveInstanceState(): Parcelable {
        val savedState = SavedState(super.onSaveInstanceState()!!)
        savedState.breadcrumbs = breadcrumbs
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        breadcrumbs = savedState.breadcrumbs
        adapter.update(createItems(breadcrumbs))
    }

    private fun createItems(breadcrumbs: List<Breadcrumb>): List<ListItem> {
        val items = ArrayList<ListItem>(breadcrumbs.size * 2 - 1)
        for ((index, breadcrumb) in breadcrumbs.withIndex()) {
            items.add(BreadcrumbItem(index.toString(), breadcrumb))

            if (index < breadcrumbs.size - 1) {
                items.add(SeparatorItem(index.toString()))
            }
        }
        return items
    }

    private class SavedState : AbsSavedState {
        lateinit var breadcrumbs: List<Breadcrumb>

        constructor(superState: Parcelable) : super(superState)

        constructor(source: Parcel, loader: ClassLoader?) : super(source, loader) {
            breadcrumbs = source.createTypedArrayList(getParcelableCreator<Breadcrumb>())!!
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeTypedList(breadcrumbs)
        }

        companion object CREATOR : Parcelable.ClassLoaderCreator<SavedState> {
            override fun createFromParcel(source: Parcel) = SavedState(source, null)

            override fun createFromParcel(source: Parcel, loader: ClassLoader?): SavedState = SavedState(source, loader)

            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }
}

typealias OnBreadcrumbClickListener = (Breadcrumb) -> Unit
