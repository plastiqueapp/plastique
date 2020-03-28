package io.plastique.deviations.tags

interface TagManager {
    fun setTags(tags: List<Tag>, animated: Boolean)

    var onTagClick: OnTagClickListener
}
