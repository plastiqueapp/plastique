package io.plastique.feed

import io.plastique.core.lists.ItemsData
import io.plastique.core.paging.StringCursor
import io.plastique.core.text.RichTextFormatter
import io.plastique.core.text.SpannedWrapper
import io.plastique.deviations.Deviation
import io.plastique.deviations.list.DeviationItemFactory
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
    private val deviationItemFactory: DeviationItemFactory,
    private val richTextFormatter: RichTextFormatter
) {
    private var matureContent: Boolean = false
    private val nextCursor = AtomicReference<StringCursor>()

    fun items(matureContent: Boolean): Observable<ItemsData> {
        this.matureContent = matureContent
        return feedRepository.getFeed(matureContent)
            .map { pagedData ->
                nextCursor.set(pagedData.nextCursor)
                val items = pagedData.value.map { createItem(it, matureContent) }
                ItemsData(items, hasMore = pagedData.nextCursor != null)
            }
    }

    fun loadMore(): Completable {
        return feedRepository.fetch(matureContent, nextCursor.get()!!)
            .doOnSuccess { cursor -> nextCursor.set(cursor.toNullable()) }
            .ignoreElement()
    }

    fun refresh(): Completable {
        return feedRepository.fetch(matureContent, null)
            .doOnSuccess { cursor -> nextCursor.set(cursor.toNullable()) }
            .ignoreElement()
    }

    private fun createItem(feedElement: FeedElement, matureContent: Boolean): FeedListItem = when (feedElement) {
        is CollectionUpdate ->
            CollectionUpdateItem(
                id = feedElement.timestamp.toString(),
                date = feedElement.timestamp,
                user = feedElement.user,
                folderId = feedElement.folder.id,
                folderName = feedElement.folder.name,
                addedCount = feedElement.addedCount,
                folderItems = emptyList())

        is FeedElement.MultipleDeviationsSubmitted -> {
            val items = feedElement.deviations.mapIndexed { index, deviation -> deviationItemFactory.create(deviation, index) }
            MultipleDeviationsItem(
                id = "${feedElement.user.id}-${feedElement.timestamp}",
                date = feedElement.timestamp,
                user = feedElement.user,
                submittedTotal = feedElement.submittedTotal,
                items = items)
        }

        is FeedElement.DeviationSubmitted -> {
            when (val data = feedElement.deviation.data) {
                is Deviation.Data.Image ->
                    ImageDeviationItem(
                        date = feedElement.timestamp,
                        user = feedElement.user,
                        deviationId = feedElement.deviation.id,
                        title = feedElement.deviation.title,
                        isFavorite = feedElement.deviation.properties.isFavorite,
                        allowsComments = feedElement.deviation.properties.allowsComments,
                        favoriteCount = feedElement.deviation.stats.favorites,
                        commentCount = feedElement.deviation.stats.comments,
                        preview = data.preview,
                        content = data.content)

                is Deviation.Data.Literature ->
                    LiteratureDeviationItem(
                        date = feedElement.timestamp,
                        user = feedElement.user,
                        deviationId = feedElement.deviation.id,
                        title = feedElement.deviation.title,
                        isFavorite = feedElement.deviation.properties.isFavorite,
                        allowsComments = feedElement.deviation.properties.allowsComments,
                        favoriteCount = feedElement.deviation.stats.favorites,
                        commentCount = feedElement.deviation.stats.comments,
                        excerpt = SpannedWrapper(richTextFormatter.format(data.excerpt)))

                is Deviation.Data.Video -> TODO()
            }
        }

        is JournalSubmitted -> {
            val data = feedElement.deviation.data as Deviation.Data.Literature
            LiteratureDeviationItem(
                date = feedElement.timestamp,
                user = feedElement.user,
                deviationId = feedElement.deviation.id,
                title = feedElement.deviation.title,
                isFavorite = feedElement.deviation.properties.isFavorite,
                allowsComments = feedElement.deviation.properties.allowsComments,
                favoriteCount = feedElement.deviation.stats.favorites,
                commentCount = feedElement.deviation.stats.comments,
                excerpt = SpannedWrapper(richTextFormatter.format(data.excerpt)))
        }

        is StatusUpdate ->
            StatusUpdateItem(
                date = feedElement.timestamp,
                user = feedElement.user,
                statusId = feedElement.status.id,
                text = SpannedWrapper(richTextFormatter.format(feedElement.status.body)),
                commentCount = feedElement.status.commentCount,
                share = feedElement.status.share.toShareUiModel(richTextFormatter, matureContent))

        is UsernameChange ->
            UsernameChangeItem(
                id = "${feedElement.user.name}-${feedElement.formerName}-${feedElement.timestamp}",
                date = feedElement.timestamp,
                user = feedElement.user,
                formerName = feedElement.formerName)
    }
}
