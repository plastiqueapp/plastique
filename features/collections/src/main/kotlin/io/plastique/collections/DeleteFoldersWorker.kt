package io.plastique.collections

import android.content.Context
import androidx.work.WorkerParameters
import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import io.plastique.core.work.CompletableWorker
import io.plastique.core.work.ListenableWorkerFactory
import io.reactivex.Completable

@AutoFactory(implementing = [ListenableWorkerFactory::class])
class DeleteFoldersWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    @Provided private val collectionFolderRepository: CollectionFolderRepository
) : CompletableWorker(appContext, workerParams) {

    override fun createCompletableWork(): Completable {
        return collectionFolderRepository.deleteMarkedFolders()
    }
}
