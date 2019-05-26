package io.plastique.collections

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import io.plastique.core.work.CommonWorkTags
import io.plastique.core.work.setInitialDelay
import io.reactivex.Completable
import org.threeten.bp.Duration
import javax.inject.Inject

class CollectionsModel @Inject constructor(
    private val collectionFolderRepository: CollectionFolderRepository,
    private val workManager: WorkManager
) {
    fun createFolder(folderName: String): Completable {
        return collectionFolderRepository.createFolder(folderName)
    }

    fun deleteFolderById(folderId: String): Completable {
        return collectionFolderRepository.markAsDeleted(folderId, true)
            .doOnComplete { scheduleDeletion() }
    }

    fun undoDeleteFolderById(messageId: String): Completable {
        return collectionFolderRepository.markAsDeleted(messageId, false)
    }

    private fun scheduleDeletion() {
        val workRequest = OneTimeWorkRequestBuilder<DeleteFoldersWorker>()
            .setConstraints(Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build())
            .setInitialDelay(DELETE_FOLDER_DELAY)
            .addTag(CommonWorkTags.CANCEL_ON_LOGOUT)
            .build()
        workManager.enqueueUniqueWork(WORK_DELETE_FOLDERS, ExistingWorkPolicy.REPLACE, workRequest)
    }

    companion object {
        private const val WORK_DELETE_FOLDERS = "collections.delete_folders"
        private val DELETE_FOLDER_DELAY = Duration.ofSeconds(15)
    }
}
