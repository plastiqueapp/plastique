package io.plastique.gallery

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import io.plastique.core.work.CommonWorkTags
import io.reactivex.Completable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GalleryModel @Inject constructor(
    private val galleryFolderRepository: GalleryFolderRepository,
    private val workManager: WorkManager
) {
    fun deleteFolderById(folderId: String): Completable {
        return galleryFolderRepository.markAsDeleted(folderId, true)
            .doOnComplete { scheduleDeletion() }
    }

    fun undoDeleteFolderById(messageId: String): Completable {
        return galleryFolderRepository.markAsDeleted(messageId, false)
    }

    private fun scheduleDeletion() {
        val workRequest = OneTimeWorkRequest.Builder(DeleteFoldersWorker::class.java)
            .setConstraints(Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build())
            .setInitialDelay(DELETE_FOLDER_DELAY, TimeUnit.MILLISECONDS)
            .addTag(CommonWorkTags.CANCEL_ON_LOGOUT)
            .build()
        workManager.enqueueUniqueWork(WORK_DELETE_FOLDERS, ExistingWorkPolicy.REPLACE, workRequest)
    }

    companion object {
        private const val WORK_DELETE_FOLDERS = "gallery.delete_folders"
        private val DELETE_FOLDER_DELAY = TimeUnit.SECONDS.toMillis(15)
    }
}
