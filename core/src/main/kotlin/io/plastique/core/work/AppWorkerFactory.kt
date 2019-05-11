package io.plastique.core.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import javax.inject.Inject

class AppWorkerFactory @Inject constructor(
    private val delegateWorkerFactories: Map<Class<*>, @JvmSuppressWildcards ListenableWorkerFactory>
) : WorkerFactory() {
    override fun createWorker(context: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker {
        val workerClass = Class.forName(workerClassName)
        val delegateWorkerFactory = delegateWorkerFactories[workerClass]
            ?: throw IllegalStateException("No ListenableWorkerFactory is provided for $workerClass")
        return delegateWorkerFactory.create(context, workerParameters)
    }
}
