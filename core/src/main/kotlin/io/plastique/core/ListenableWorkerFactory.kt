package io.plastique.core

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters

interface ListenableWorkerFactory {
    fun create(context: Context, workerParams: WorkerParameters): ListenableWorker
}
