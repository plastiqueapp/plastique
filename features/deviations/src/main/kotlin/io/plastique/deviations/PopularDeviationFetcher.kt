package io.plastique.deviations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.deviations.DeviationService
import io.plastique.api.deviations.TimeRange
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

    val category: Category = Category.ALL,
    val timeRange: TimeRange = TimeRange.Days3
) : FetchParams {
    override fun with(showMatureContent: Boolean, showLiterature: Boolean): FetchParams {
        return copy(showMatureContent = showMatureContent, showLiterature = showLiterature)
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
                categoryPath = params.category.pathOrNull,
                matureContent = params.showMatureContent,
                offset = offset,
                limit = 50)
                .map { deviationList ->
                    FetchResult(
                            deviations = deviationList.deviations,
                            nextCursor = if (deviationList.hasMore) OffsetCursor(deviationList.nextOffset!!) else null,
                            replaceExisting = offset == 0)
                }
    }
}
