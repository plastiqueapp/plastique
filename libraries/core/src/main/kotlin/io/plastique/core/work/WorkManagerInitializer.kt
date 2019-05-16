package io.plastique.core.work

import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import io.plastique.core.init.Initializer
import javax.inject.Inject

class WorkManagerInitializer @Inject constructor(
    private val context: Context,
    private val workerFactory: WorkerFactory
) : Initializer() {

    override fun initialize() {
        WorkManager.initialize(context, Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build())
    }
}
