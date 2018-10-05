package io.plastique.deviations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.deviations.DeviationService
import io.plastique.core.paging.OffsetCursor
import io.plastique.deviations.categories.Category
import io.reactivex.Single
import javax.inject.Inject

@JsonClass(generateAdapter = true)
data class UndiscoveredParams(
    @Json(name = "show_mature")
    override val showMatureContent: Boolean = false,

    @Json(name = "show_literature")
    override val showLiterature: Boolean = false,

    val category: Category = Category.ALL
) : FetchParams {
    override fun isSameAs(params: FetchParams): Boolean {
        if (params !is UndiscoveredParams) return false
        return showMatureContent == params.showMatureContent &&
                showLiterature == params.showLiterature &&
                category == params.category
    }

    override fun with(showMatureContent: Boolean, showLiterature: Boolean): FetchParams {
        return copy(showMatureContent = showMatureContent, showLiterature = showLiterature)
    }
}

class UndiscoveredDeviationFetcher @Inject constructor(
    private val deviationService: DeviationService
) : DeviationFetcher<UndiscoveredParams, OffsetCursor> {
    override fun getCacheKey(params: UndiscoveredParams): String = "undiscovered"

    override fun createMetadataSerializer(): DeviationCacheMetadataSerializer =
            DeviationCacheMetadataSerializer(paramsType = UndiscoveredParams::class.java, cursorType = OffsetCursor::class.java)

    override fun fetch(params: UndiscoveredParams, cursor: OffsetCursor?): Single<FetchResult<OffsetCursor>> {
        val offset = cursor?.offset ?: 0
        return deviationService.getUndiscoveredDeviations(
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
