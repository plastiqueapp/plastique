@file:Suppress("NOTHING_TO_INLINE")

package io.plastique.core.work

import androidx.work.OneTimeWorkRequest
import org.threeten.bp.Duration
import java.util.concurrent.TimeUnit

inline fun OneTimeWorkRequest.Builder.setInitialDelay(duration: Duration): OneTimeWorkRequest.Builder {
    return setInitialDelay(duration.toMillis(), TimeUnit.MILLISECONDS)
}
