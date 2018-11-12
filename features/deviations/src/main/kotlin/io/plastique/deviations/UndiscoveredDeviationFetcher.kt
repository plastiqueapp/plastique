package io.plastique.deviations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.deviations.DeviationService
import io.plastique.core.paging.OffsetCursor
import io.plastique.core.paging.nextCursor
import io.plastique.deviations.categories.Category
import io.reactivex.Single
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

@Parcelize
@JsonClass(generateAdapter = true)
data class UndiscoveredParams(
    @Json(name = "show_mature")
    override val showMatureContent: Boolean = false,

    @Json(name = "show_literature")
    override val showLiterature: Boolean = false,

    val category: Category = Category.ALL
) : FetchParams {
    override fun with(showMatureContent: Boolean, showLiterature: Boolean): FetchParams {
        return copy(showMatureContent = showMatureContent, showLiterature = showLiterature)
    }
}

class UndiscoveredDeviationFetcher @Inject constructor(
    private val deviationService: DeviationService
) : DeviationFetcher<UndiscoveredParams, OffsetCursor> {
    override fun getCacheKey(params: UndiscoveredParams): String = "undiscovered-deviations"

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
                            deviations = deviationList.results,
                            nextCursor = deviationList.nextCursor,
                            replaceExisting = offset == 0)
                }
    }
}
