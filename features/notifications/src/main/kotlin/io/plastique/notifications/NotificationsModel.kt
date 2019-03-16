package io.plastique.notifications

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import io.plastique.core.lists.ItemsData
import io.plastique.core.lists.ListItem
import io.plastique.core.paging.StringCursor
import io.plastique.core.work.CommonWorkTags
import io.reactivex.Completable
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

class NotificationsModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val workManager: WorkManager
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

    fun deleteMessageById(messageId: String): Completable {
        return messageRepository.markAsDeleted(messageId, true)
                .doOnComplete {
                    val workRequest = OneTimeWorkRequest.Builder(DeleteMessagesWorker::class.java)
                            .setConstraints(Constraints.Builder()
                                    .setRequiredNetworkType(NetworkType.CONNECTED)
                                    .build())
                            .setInitialDelay(10, TimeUnit.SECONDS)
                            .addTag(CommonWorkTags.CANCEL_ON_LOGOUT)
                            .build()
                    workManager.enqueueUniqueWork(WORK_DELETE_MESSAGES, ExistingWorkPolicy.REPLACE, workRequest)
                }
    }

    fun undoDeleteMessageById(messageId: String): Completable {
        return messageRepository.markAsDeleted(messageId, false)
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

        is Message.BadgeGiven -> BadgeGivenItem(
                messageId = message.id,
                time = message.time,
                user = message.user,
                text = message.text)

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

    companion object {
        private const val WORK_DELETE_MESSAGES = "delete-messages"
    }
}
