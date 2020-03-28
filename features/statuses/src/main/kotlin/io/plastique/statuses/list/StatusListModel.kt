package io.plastique.statuses.list

import io.plastique.core.lists.ItemsData
import io.plastique.core.paging.OffsetCursor
import io.plastique.core.text.RichTextFormatter
import io.plastique.core.text.SpannedWrapper
import io.plastique.statuses.Status
import io.plastique.statuses.StatusListLoadParams
import io.plastique.statuses.StatusRepositoryImpl
import io.plastique.statuses.createActionsState
import io.plastique.statuses.toShareUiModel
import io.reactivex.Completable
import io.reactivex.Observable
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

class StatusListModel @Inject constructor(
    private val statusRepositoryImpl: StatusRepositoryImpl,
    private val richTextFormatter: RichTextFormatter
) {
    private val params = AtomicReference<StatusListLoadParams>()
    private val nextCursor = AtomicReference<OffsetCursor>()

    fun getItems(params: StatusListLoadParams): Observable<ItemsData> {
        this.params.set(params)
        return statusRepositoryImpl.getStatuses(params)
            .map { pagedData ->
                val items = pagedData.value.map { createStatusItem(it, params.matureContent) }
                ItemsData(items, pagedData.hasMore)
            }
    }

    fun loadMore(): Completable {
        return statusRepositoryImpl.fetch(params.get(), nextCursor.get()!!)
            .doOnSuccess { cursor -> nextCursor.set(cursor.toNullable()) }
            .ignoreElement()
    }

    fun refresh(): Completable {
        return statusRepositoryImpl.fetch(params.get(), null)
            .doOnSuccess { cursor -> nextCursor.set(cursor.toNullable()) }
            .ignoreElement()
    }

    private fun createStatusItem(status: Status, matureContent: Boolean): StatusItem {
        val share = status.share.toShareUiModel(richTextFormatter, matureContent)
        return StatusItem(
            statusId = status.id,
            author = status.author,
            date = status.date,
            statusText = SpannedWrapper(richTextFormatter.format(status.body)),
            share = share,
            actionsState = status.createActionsState())
    }
}
