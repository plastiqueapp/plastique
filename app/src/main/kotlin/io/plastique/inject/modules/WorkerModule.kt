package io.plastique.inject.modules

import androidx.work.WorkerFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.Multibinds
import io.plastique.core.AppWorkerFactory
import io.plastique.core.ListenableWorkerFactory

@Module
interface WorkerModule {
    @Binds
    fun bindWorkerFactory(impl: AppWorkerFactory): WorkerFactory

    @Multibinds
    fun workerFactories(): Map<Class<*>, ListenableWorkerFactory>
}
