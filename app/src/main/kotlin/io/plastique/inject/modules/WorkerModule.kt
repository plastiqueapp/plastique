package io.plastique.inject.modules

import androidx.work.WorkerFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import dagger.multibindings.Multibinds
import io.plastique.core.work.AppWorkerFactory
import io.plastique.core.work.ListenableWorkerFactory
import io.plastique.notifications.DeleteMessagesWorker
import io.plastique.notifications.DeleteMessagesWorkerFactory
import io.plastique.collections.DeleteFoldersWorker as DeleteCollectionFoldersWorker
import io.plastique.collections.DeleteFoldersWorkerFactory as DeleteCollectionFoldersWorkerFactory
import io.plastique.gallery.DeleteFoldersWorker as DeleteGalleryFoldersWorker
import io.plastique.gallery.DeleteFoldersWorkerFactory as DeleteGalleryFoldersWorkerFactory

@Module
interface WorkerModule {
    @Binds
    fun bindWorkerFactory(impl: AppWorkerFactory): WorkerFactory

    @Multibinds
    fun workerFactories(): Map<Class<*>, ListenableWorkerFactory>

    @Binds
    @IntoMap
    @ClassKey(DeleteCollectionFoldersWorker::class)
    fun bindDeleteCollectionFoldersWorkerFactory(impl: DeleteCollectionFoldersWorkerFactory): ListenableWorkerFactory

    @Binds
    @IntoMap
    @ClassKey(DeleteGalleryFoldersWorker::class)
    fun bindDeleteGalleryFoldersWorkerFactory(impl: DeleteGalleryFoldersWorkerFactory): ListenableWorkerFactory

    @Binds
    @IntoMap
    @ClassKey(DeleteMessagesWorker::class)
    fun bindDeleteMessagesWorkerFactory(impl: DeleteMessagesWorkerFactory): ListenableWorkerFactory
}
