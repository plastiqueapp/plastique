package io.plastique.core.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters

interface ListenableWorkerFactory {
    fun create(context: Context, workerParams: WorkerParameters): ListenableWorker
}
