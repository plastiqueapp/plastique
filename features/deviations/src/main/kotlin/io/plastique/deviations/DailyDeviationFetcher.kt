package io.plastique.deviations

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.plastique.api.deviations.DeviationService
import io.plastique.core.paging.DateCursor
import io.reactivex.Single
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
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
        val date = cursor?.date ?: LocalDate.now(ZoneOffset.UTC)
        return deviationService.getDailyDeviations(date.format(DATE_FORMAT), params.showMatureContent)
                .map { deviationList ->
                    FetchResult(
                            deviations = deviationList.results,
                            nextCursor = DateCursor(date.minusDays(1)),
                            replaceExisting = cursor == null)
                }
    }

    companion object {
        private val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    }
}
