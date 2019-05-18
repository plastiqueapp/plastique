package io.plastique.deviations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.common.ListResult
import io.plastique.api.deviations.DeviationDto
import io.plastique.api.deviations.DeviationService
import io.plastique.core.paging.DateCursor
import io.reactivex.Single
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject

@Parcelize
@JsonClass(generateAdapter = true)
data class DailyParams(
    @Json(name = "show_mature")
    override val showMatureContent: Boolean = false,

    @Json(name = "show_literature")
    override val showLiterature: Boolean = false
) : FetchParams {
    override fun with(showMatureContent: Boolean, showLiterature: Boolean): FetchParams {
        return copy(showMatureContent = showMatureContent, showLiterature = showLiterature)
    }
}

class DailyDeviationFetcher @Inject constructor(
    private val deviationService: DeviationService
) : DeviationFetcher<DailyParams, DateCursor> {
    override fun getCacheKey(params: DailyParams): String = "daily-deviations"

    override fun createMetadataSerializer(): DeviationCacheMetadataSerializer =
        DeviationCacheMetadataSerializer(paramsType = DailyParams::class.java, cursorType = DateCursor::class.java)

    override fun fetch(params: DailyParams, cursor: DateCursor?): Single<FetchResult<DateCursor>> {
        val date = cursor?.date
        return deviationService.getDailyDeviations(date?.format(DateTimeFormatter.ISO_LOCAL_DATE), params.showMatureContent)
            .map { deviationList ->
                val previousDate = (date ?: getDailyDeviationsDate(deviationList)).minusDays(1)
                FetchResult(
                    deviations = deviationList.results,
                    nextCursor = DateCursor(previousDate),
                    replaceExisting = cursor == null)
            }
    }

    private fun getDailyDeviationsDate(deviationList: ListResult<DeviationDto>): LocalDate {
        return deviationList.results.first().dailyDeviation!!.date.toLocalDate()
    }
}
