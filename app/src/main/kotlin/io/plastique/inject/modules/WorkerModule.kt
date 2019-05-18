package io.plastique.inject.modules

import androidx.work.WorkerFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import dagger.multibindings.Multibinds
import io.plastique.collections.DeleteFoldersWorker
import io.plastique.collections.DeleteFoldersWorkerFactory
import io.plastique.core.work.AppWorkerFactory
import io.plastique.core.work.ListenableWorkerFactory
import io.plastique.notifications.DeleteMessagesWorker
import io.plastique.notifications.DeleteMessagesWorkerFactory

@Module
interface WorkerModule {
    @Binds
    fun bindWorkerFactory(impl: AppWorkerFactory): WorkerFactory

    @Multibinds
    fun workerFactories(): Map<Class<*>, ListenableWorkerFactory>

    @Binds
    @IntoMap
    @ClassKey(DeleteFoldersWorker::class)
    fun bindDeleteFoldersWorkerFactory(impl: DeleteFoldersWorkerFactory): ListenableWorkerFactory

    @Binds
    @IntoMap
    @ClassKey(DeleteMessagesWorker::class)
    fun bindDeleteMessagesWorkerFactory(impl: DeleteMessagesWorkerFactory): ListenableWorkerFactory
}
