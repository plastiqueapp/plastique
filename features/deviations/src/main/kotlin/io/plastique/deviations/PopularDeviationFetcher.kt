package io.plastique.deviations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.deviations.DeviationService
import io.plastique.api.deviations.TimeRange
import io.plastique.api.nextCursor
import io.plastique.core.paging.OffsetCursor
import io.plastique.deviations.categories.Category
import io.reactivex.Single
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

@Parcelize
@JsonClass(generateAdapter = true)
data class PopularParams(
    @Json(name = "show_mature")
    override val showMatureContent: Boolean = false,

    @Json(name = "show_literature")
    override val showLiterature: Boolean = false,

    @Transient
    var category: Category = Category.ALL,

    @Json(name = "category_path")
    val categoryPath: String = category.path,

    @Json(name = "time_range")
    val timeRange: TimeRange = TimeRange.Days3
) : FetchParams {
    override fun with(showMatureContent: Boolean, showLiterature: Boolean): FetchParams {
        return copy(showMatureContent = showMatureContent, showLiterature = showLiterature)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PopularParams

        if (showMatureContent != other.showMatureContent) return false
        if (showLiterature != other.showLiterature) return false
        if (categoryPath != other.categoryPath) return false
        if (timeRange != other.timeRange) return false

        return true
    }

    override fun hashCode(): Int {
        var result = showMatureContent.hashCode()
        result = 31 * result + showLiterature.hashCode()
        result = 31 * result + categoryPath.hashCode()
        result = 31 * result + timeRange.hashCode()
        return result
    }
}

class PopularDeviationFetcher @Inject constructor(
    private val deviationService: DeviationService
) : DeviationFetcher<PopularParams, OffsetCursor> {
    override fun getCacheKey(params: PopularParams): String = "popular-deviations"

    override fun createMetadataSerializer(): DeviationCacheMetadataSerializer =
        DeviationCacheMetadataSerializer(paramsType = PopularParams::class.java, cursorType = OffsetCursor::class.java)

    override fun fetch(params: PopularParams, cursor: OffsetCursor?): Single<FetchResult<OffsetCursor>> {
        val offset = cursor?.offset ?: 0
        return deviationService.getPopularDeviations(
            timeRange = params.timeRange,
            categoryPath = normalizeCategoryPath(params.categoryPath),
            matureContent = params.showMatureContent,
            offset = offset,
            limit = 50)
            .map { deviationList ->
                FetchResult(
                    deviations = deviationList.results,
                    nextCursor = deviationList.nextCursor,
                    replaceExisting = offset == 0)
            }
    }
}
