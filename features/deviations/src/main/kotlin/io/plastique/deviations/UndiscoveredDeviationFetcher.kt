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

    @Transient
    var category: Category = Category.ALL,

    @Json(name = "category_path")
    val categoryPath: String = category.path
) : FetchParams {
    override fun with(showMatureContent: Boolean, showLiterature: Boolean): FetchParams {
        return copy(showMatureContent = showMatureContent, showLiterature = showLiterature)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UndiscoveredParams

        if (showMatureContent != other.showMatureContent) return false
        if (showLiterature != other.showLiterature) return false
        if (categoryPath != other.categoryPath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = showMatureContent.hashCode()
        result = 31 * result + showLiterature.hashCode()
        result = 31 * result + categoryPath.hashCode()
        return result
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
