package io.plastique.deviations.tags

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import androidx.customview.view.AbsSavedState
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.github.technoir42.android.extensions.getParcelableCreator
import com.google.android.flexbox.FlexboxLayoutManager
import io.plastique.core.lists.ListDiffCallback
import io.plastique.deviations.R

class TagsView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs), TagManager {
    private val adapter: TagsAdapter
    private val tags = mutableListOf<Tag>()

    override var onTagClickListener: OnTagClickListener?
        get() = adapter.onTagClickListener
        set(value) {
            adapter.onTagClickListener = value
        }

    init {
        overScrollMode = OVER_SCROLL_NEVER

        val a = context.obtainStyledAttributes(attrs, R.styleable.TagsView, 0, 0)
        val tagBackgroundResId = a.getResourceId(R.styleable.TagsView_tagBackground, 0)
        val tagTextAppearance = a.getResourceId(R.styleable.TagsView_tagTextAppearance, 0)
        val tagMargin = a.getDimensionPixelOffset(R.styleable.TagsView_tagMargin, 0)
        a.recycle()

        layoutManager = FlexboxLayoutManager(context)
        adapter = TagsAdapter(tagBackgroundResId, tagTextAppearance, tagMargin)
        adapter.items = tags
        setAdapter(adapter)
    }

    override fun setTags(tags: List<Tag>, animated: Boolean) {
        if (animated) {
            val diffResult = DiffUtil.calculateDiff(ListDiffCallback(this.tags, tags, TagDiffCallback(this.tags, tags)))
            this.tags.clear()
            this.tags.addAll(tags)
            diffResult.dispatchUpdatesTo(adapter)
        } else if (this.tags != tags) {
            this.tags.clear()
            this.tags.addAll(tags)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val savedState = SavedState(super.onSaveInstanceState()!!)
        savedState.tags = tags
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        setTags(savedState.tags, false)
    }

    class TagDiffCallback(
        private val oldTags: List<Tag>,
        private val newTags: List<Tag>
    ) : DiffUtil.ItemCallback<Tag>() {

        override fun areItemsTheSame(oldItem: Tag, newItem: Tag): Boolean =
            oldItem.type == newItem.type && getTagIndex(oldItem, oldTags) == getTagIndex(newItem, newTags)

        override fun areContentsTheSame(oldItem: Tag, newItem: Tag): Boolean =
            oldItem == newItem

        private fun getTagIndex(tag: Tag, tags: List<Tag>): Int {
            var index = 0
            for (t in tags) {
                if (t === tag) {
                    return index
                } else if (t.type == tag.type) {
                    index++
                }
            }
            throw IllegalArgumentException("Tag $tag was not found in the list")
        }
    }

    private class SavedState : AbsSavedState {
        lateinit var tags: List<Tag>

        constructor(superState: Parcelable) : super(superState)

        constructor(source: Parcel, loader: ClassLoader?) : super(source, loader) {
            tags = source.createTypedArrayList(getParcelableCreator<Tag>())!!
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeTypedList(tags)
        }

        companion object CREATOR : Parcelable.ClassLoaderCreator<SavedState> {
            override fun createFromParcel(source: Parcel): SavedState = SavedState(source, null)

            override fun createFromParcel(source: Parcel, loader: ClassLoader?): SavedState = SavedState(source, loader)

            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }
}
