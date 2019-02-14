package io.plastique.notifications

import io.plastique.core.lists.ItemsData
import io.plastique.core.lists.ListItem
import io.plastique.core.paging.StringCursor
import io.reactivex.Completable
import io.reactivex.Observable
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

class NotificationsModel @Inject constructor(
    private val messageRepository: MessageRepository
) {
    private val nextCursor = AtomicReference<StringCursor>()

    fun items(): Observable<ItemsData> {
        return messageRepository.getMessages()
                .map { pagedData ->
                    nextCursor.set(pagedData.nextCursor)
                    val items = pagedData.value.map { createItem(it) }
                    ItemsData(items, hasMore = pagedData.hasMore)
                }
    }

    fun loadMore(): Completable {
        return messageRepository.fetch(nextCursor.get()!!)
                .doOnSuccess { cursor -> nextCursor.set(cursor.orNull()) }
                .ignoreElement()
    }

    fun refresh(): Completable {
        return messageRepository.fetch(null)
                .doOnSuccess { cursor -> nextCursor.set(cursor.orNull()) }
                .ignoreElement()
    }

    private fun createItem(message: Message): ListItem = when (message) {
        is Message.AddToCollection -> AddToCollectionItem(
                messageId = message.id,
                time = message.time,
                user = message.user,
                deviationId = message.deviation.id,
                deviationTitle = message.deviation.title,
                folderId = message.folder.id,
                folderName = message.folder.name)

        is Message.Favorite -> FavoriteItem(
                messageId = message.id,
                time = message.time,
                user = message.user,
                deviationId = message.deviation.id,
                deviationTitle = message.deviation.title)

        is Message.Watch -> WatchItem(
                messageId = message.id,
                time = message.time,
                user = message.user)
    }
}
