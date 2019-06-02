package io.plastique.notifications

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import io.plastique.core.lists.ItemsData
import io.plastique.core.lists.ListItem
import io.plastique.core.paging.StringCursor
import io.plastique.core.work.CommonWorkTags
import io.plastique.core.work.setInitialDelay
import io.reactivex.Completable
import io.reactivex.Observable
import org.threeten.bp.Duration
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
            .doOnSuccess { cursor -> nextCursor.set(cursor.toNullable()) }
            .ignoreElement()
    }

    fun refresh(): Completable {
        return messageRepository.fetch(null)
            .doOnSuccess { cursor -> nextCursor.set(cursor.toNullable()) }
            .ignoreElement()
    }

    fun deleteMessageById(messageId: String): Completable {
        return messageRepository.markAsDeleted(messageId, true)
            .doOnComplete { scheduleDeletion() }
    }

    fun undoDeleteMessageById(messageId: String): Completable {
        return messageRepository.markAsDeleted(messageId, false)
    }

    private fun scheduleDeletion() {
        val workRequest = OneTimeWorkRequestBuilder<DeleteMessagesWorker>()
            .setConstraints(Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build())
            .setInitialDelay(DELETE_MESSAGE_DELAY)
            .addTag(CommonWorkTags.CANCEL_ON_LOGOUT)
            .build()
        workManager.enqueueUniqueWork(WORK_DELETE_MESSAGES, ExistingWorkPolicy.REPLACE, workRequest)
    }

    private fun createItem(message: Message): ListItem = when (message.data) {
        is Message.Data.AddToCollection -> AddToCollectionItem(
            messageId = message.id,
            time = message.time,
            user = message.user,
            deviationId = message.data.deviation.id,
            deviationTitle = message.data.deviation.title,
            folderId = message.data.folder.id,
            folderName = message.data.folder.name)

        is Message.Data.BadgeGiven -> BadgeGivenItem(
            messageId = message.id,
            time = message.time,
            user = message.user,
            text = message.data.text)

        is Message.Data.Favorite -> FavoriteItem(
            messageId = message.id,
            time = message.time,
            user = message.user,
            deviationId = message.data.deviation.id,
            deviationTitle = message.data.deviation.title)

        Message.Data.Watch -> WatchItem(
            messageId = message.id,
            time = message.time,
            user = message.user)
    }

    companion object {
        private const val WORK_DELETE_MESSAGES = "notifications.delete_messages"
        private val DELETE_MESSAGE_DELAY = Duration.ofSeconds(15)
    }
}
