package io.plastique.core.work

import androidx.work.WorkManager
import javax.inject.Inject

class WorkerCleaner @Inject constructor(
    private val workManager: WorkManager
) {
    fun clean() {
        workManager.cancelAllWorkByTag(CommonWorkTags.CANCEL_ON_LOGOUT)
    }
}
