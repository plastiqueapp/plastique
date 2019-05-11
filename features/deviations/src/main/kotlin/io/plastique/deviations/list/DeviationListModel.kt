package io.plastique.deviations.list

import io.plastique.core.lists.ItemsData
import io.plastique.core.lists.ListItem
import io.plastique.core.text.RichTextFormatter
import io.plastique.core.text.SpannedWrapper
import io.plastique.deviations.DailyParams
import io.plastique.deviations.Deviation
import io.plastique.deviations.DeviationDataSource
import io.plastique.deviations.FetchParams
import io.reactivex.Completable
import io.reactivex.Observable
import org.threeten.bp.LocalDate
import javax.inject.Inject

class DeviationListModel @Inject constructor(
    private val dataSource: DeviationDataSource,
    private val richTextFormatter: RichTextFormatter
) {
    fun getItems(params: FetchParams): Observable<ItemsData> {
        return dataSource.getData(params)
            .map { pagedData ->
                val items = createItems(pagedData.value, params is DailyParams)
                ItemsData(items = items, hasMore = pagedData.hasMore)
            }
    }

    fun loadMore(): Completable {
        return dataSource.loadMore()
    }

    fun refresh(): Completable {
        return dataSource.refresh()
    }

    private fun createItems(deviations: List<Deviation>, daily: Boolean): List<ListItem> {
        var index = 0
        return if (daily) {
            val items = ArrayList<ListItem>(deviations.size + 1)
            var prevDate: LocalDate? = null
            for (deviation in deviations) {
                val date = deviation.dailyDeviation!!.date.toLocalDate()
                if (date != prevDate) {
                    items += DateItem(date)
                    prevDate = date
                    index = 0
                }
                items += createDeviationItem(deviation, index++)
            }
            items
        } else {
            deviations.map { deviation -> createDeviationItem(deviation, index++) }
        }
    }

    private fun createDeviationItem(deviation: Deviation, index: Int): DeviationItem = if (deviation.isLiterature) {
        LiteratureDeviationItem(deviation, index = index, excerpt = SpannedWrapper(richTextFormatter.format(deviation.excerpt!!)))
    } else {
        ImageDeviationItem(deviation, index = index)
    }
}
