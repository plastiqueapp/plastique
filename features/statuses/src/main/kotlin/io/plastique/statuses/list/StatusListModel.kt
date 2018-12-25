package io.plastique.statuses.list

import io.plastique.core.lists.ItemsData
import io.plastique.core.paging.OffsetCursor
import io.plastique.core.text.RichTextFormatter
import io.plastique.core.text.SpannedWrapper
import io.plastique.statuses.Status
import io.plastique.statuses.StatusRepositoryImpl
import io.plastique.statuses.toShareUiModel
import io.reactivex.Completable
import io.reactivex.Observable
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

class StatusListModel @Inject constructor(
    private val statusRepositoryImpl: StatusRepositoryImpl,
    private val richTextFormatter: RichTextFormatter
) {
    private val username = AtomicReference<String>()
    private val nextCursor = AtomicReference<OffsetCursor>()

    fun getItems(username: String): Observable<ItemsData> {
        this.username.set(username)
        return statusRepositoryImpl.getStatusesByUsername(username)
                .map { pagedData ->
                    val items = pagedData.value.map { createStatusItem(it) }
                    ItemsData(items, pagedData.hasMore)
                }
    }

    fun loadMore(): Completable {
        return statusRepositoryImpl.fetch(username.get(), nextCursor.get()!!)
                .doOnSuccess { cursor -> nextCursor.set(cursor.orNull()) }
                .ignoreElement()
    }

    fun refresh(): Completable {
        return statusRepositoryImpl.fetch(username.get(), null)
                .doOnSuccess { cursor -> nextCursor.set(cursor.orNull()) }
                .ignoreElement()
    }

    private fun createStatusItem(status: Status): StatusItem = StatusItem(
            statusId = status.id,
            author = status.author,
            date = status.date,
            statusText = SpannedWrapper(richTextFormatter.format(status.body)),
            commentCount = status.commentCount,
            share = status.share.toShareUiModel(richTextFormatter))
}
