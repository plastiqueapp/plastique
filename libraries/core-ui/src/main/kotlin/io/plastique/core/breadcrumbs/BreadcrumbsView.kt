package io.plastique.core.breadcrumbs

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.customview.view.AbsSavedState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.technoir42.android.extensions.getParcelableCreator
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

    var breadcrumbs: List<Breadcrumb> = emptyList()
        set(value) {
            if (field == value) return
            val scrollToEnd = field.size < value.size
            field = value.toList()
            val items = createItems(field)
            adapter.update(items)
            if (scrollToEnd) {
                scrollToPosition(items.size - 1)
            }
        }

    init {
        overScrollMode = View.OVER_SCROLL_NEVER

        val a = context.obtainStyledAttributes(attrs, R.styleable.BreadcrumbsView, defStyleAttr, 0)
        val breadcrumbLayoutId = a.getResourceId(R.styleable.BreadcrumbsView_breadcrumbLayoutId, 0)
        val separatorDrawableResId = a.getResourceId(R.styleable.BreadcrumbsView_separatorDrawable, 0)
        a.recycle()

        check(breadcrumbLayoutId != 0) { "Required attribute breadcrumbLayoutId is not set" }

        adapter = BreadcrumbsAdapter(breadcrumbLayoutId, separatorDrawableResId)
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        setAdapter(adapter)
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
        breadcrumbs.forEachIndexed { index, breadcrumb ->
            val id = index.toString()
            items += BreadcrumbItem(id, breadcrumb)

            if (index < breadcrumbs.size - 1) {
                items += SeparatorItem(id)
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
