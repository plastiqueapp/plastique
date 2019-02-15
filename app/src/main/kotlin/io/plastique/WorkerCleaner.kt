package io.plastique

import androidx.work.WorkManager
import io.plastique.core.session.OnLogoutListener
import io.plastique.notifications.DeleteMessagesWorker
import javax.inject.Inject

class WorkerCleaner @Inject constructor(
    private val workManager: WorkManager
) : OnLogoutListener {

    override fun onLogout() {
        workManager.cancelUniqueWork(DeleteMessagesWorker.WORK_NAME)
    }
}
