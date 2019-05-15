package io.plastique.core.work

import androidx.work.WorkManager
import io.plastique.core.session.OnLogoutListener
import javax.inject.Inject

class WorkerCleaner @Inject constructor(
    private val workManager: WorkManager
) : OnLogoutListener {

    override fun onLogout() {
        workManager.cancelAllWorkByTag(CommonWorkTags.CANCEL_ON_LOGOUT)
    }
}
