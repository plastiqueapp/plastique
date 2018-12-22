package io.plastique.feed

import io.plastique.core.lists.ItemsData
import io.plastique.core.lists.ListItem
import io.plastique.core.paging.StringCursor
import io.plastique.core.text.RichTextFormatter
import io.plastique.core.text.SpannedWrapper
import io.plastique.deviations.Deviation
import io.plastique.deviations.list.ImageDeviationItem
import io.plastique.feed.FeedElement.CollectionUpdate
import io.plastique.feed.FeedElement.JournalSubmitted
import io.plastique.feed.FeedElement.StatusUpdate
import io.plastique.feed.FeedElement.UsernameChange
import io.plastique.statuses.toShareUiModel
import io.reactivex.Completable
import io.reactivex.Observable
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

class FeedModel @Inject constructor(
    private val feedRepository: FeedRepository,
    private val richTextFormatter: RichTextFormatter
) {
    private var matureContent: Boolean = false
    private val nextCursor = AtomicReference<StringCursor>()

    fun items(matureContent: Boolean): Observable<ItemsData> {
        this.matureContent = matureContent
        return feedRepository.getFeed(matureContent)
                .map { pagedData ->
                    nextCursor.set(pagedData.nextCursor)
                    val items = pagedData.value.map { createItem(it) }
                    ItemsData(items, hasMore = pagedData.nextCursor != null)
                }
    }

    fun loadMore(): Completable {
        return feedRepository.fetch(matureContent, nextCursor.get()!!)
                .doOnSuccess { cursor -> nextCursor.set(cursor.orNull()) }
                .ignoreElement()
    }

    fun refresh(): Completable {
        return feedRepository.fetch(matureContent, null)
                .doOnSuccess { cursor -> nextCursor.set(cursor.orNull()) }
                .ignoreElement()
    }

    private fun createItem(feedElement: FeedElement): ListItem = when (feedElement) {
        is CollectionUpdate -> CollectionUpdateItem(
                id = feedElement.timestamp.toString(),
                date = feedElement.timestamp,
                user = feedElement.user,
                folderId = feedElement.folder.id,
                folderName = feedElement.folder.name,
                addedCount = feedElement.addedCount,
                folderItems = emptyList())

        is FeedElement.MultipleDeviationsSubmitted -> {
            var index = 0
            MultipleDeviationsItem(
                    id = "${feedElement.user.id}-${feedElement.timestamp}",
                    date = feedElement.timestamp,
                    user = feedElement.user,
                    submittedTotal = feedElement.submittedTotal,
                    items = feedElement.deviations.map { deviation -> createDeviationItem(deviation, index++) })
        }

        is FeedElement.DeviationSubmitted -> if (feedElement.deviation.isLiterature) {
            LiteratureDeviationItem(
                    date = feedElement.timestamp,
                    user = feedElement.user,
                    deviation = feedElement.deviation,
                    excerpt = SpannedWrapper(richTextFormatter.format(feedElement.deviation.excerpt!!)))
        } else {
            ImageDeviationItem(
                    date = feedElement.timestamp,
                    user = feedElement.user,
                    deviation = feedElement.deviation)
        }

        is JournalSubmitted -> LiteratureDeviationItem(
                date = feedElement.timestamp,
                user = feedElement.user,
                deviation = feedElement.deviation,
                excerpt = SpannedWrapper(richTextFormatter.format(feedElement.deviation.excerpt!!)))

        is StatusUpdate -> StatusUpdateItem(
                date = feedElement.timestamp,
                user = feedElement.user,
                statusId = feedElement.status.id,
                text = SpannedWrapper(richTextFormatter.format(feedElement.status.body)),
                commentCount = feedElement.status.commentCount,
                share = feedElement.status.share.toShareUiModel(richTextFormatter))

        is UsernameChange -> UsernameChangeItem(
                id = "${feedElement.user.name}-${feedElement.formerName}-${feedElement.timestamp}",
                date = feedElement.timestamp,
                user = feedElement.user,
                formerName = feedElement.formerName)
    }

    private fun createDeviationItem(deviation: Deviation, index: Int): io.plastique.deviations.list.DeviationItem = if (deviation.isLiterature) {
        io.plastique.deviations.list.LiteratureDeviationItem(deviation, index = index, excerpt = SpannedWrapper(richTextFormatter.format(deviation.excerpt!!)))
    } else {
        ImageDeviationItem(deviation, index = index)
    }
}
