package io.plastique.deviations.tags

import android.content.Context
import androidx.annotation.StringRes
import io.plastique.api.deviations.TimeRange
import io.plastique.deviations.R
import io.plastique.deviations.categories.Category
import javax.inject.Inject

class TagFactory @Inject constructor(private val context: Context) {
    fun createCategoryTags(category: Category): List<Tag> {
        return if (category.parent != null) {
            val tags = mutableListOf<Tag>()
            var c: Category? = category
            while (c?.parent != null) {
                tags.add(0, createCategoryTag(c))
                c = c.parent
            }
            tags
        } else {
            listOf(createCategoryTag(category))
        }
    }

    private fun createCategoryTag(category: Category): Tag {
        return Tag(Tag.TYPE_CATEGORY, category.title, category)
    }

    fun createTimeRangeTag(timeRange: TimeRange): Tag {
        return Tag(Tag.TYPE_TIME_RANGE, context.getString(getTimeRangeTagTextId(timeRange)))
    }

    @StringRes
    private fun getTimeRangeTagTextId(timeRange: TimeRange): Int = when (timeRange) {
        TimeRange.Hours8 -> R.string.deviations_popular_time_range_8_hours
        TimeRange.Hours24 -> R.string.deviations_popular_time_range_24_hours
        TimeRange.Days3 -> R.string.deviations_popular_time_range_3_days
        TimeRange.Week -> R.string.deviations_popular_time_range_1_week
        TimeRange.Month -> R.string.deviations_popular_time_range_1_month
        TimeRange.AllTime -> R.string.deviations_popular_time_range_all_time
    }
}
