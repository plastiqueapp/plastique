package io.plastique.notifications

import android.content.Context
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import io.plastique.core.exceptions.isRetryable
import io.plastique.core.work.ListenableWorkerFactory
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@AutoFactory(implementing = [ListenableWorkerFactory::class])
class DeleteMessagesWorker @Inject constructor(
    context: Context,
    workerParams: WorkerParameters,
    @Provided private val messageRepository: MessageRepository
) : RxWorker(context, workerParams) {

    override fun createWork(): Single<Result> {
        Timber.tag(LOG_TAG).d("createWork")
        return messageRepository.deleteMarkedMessages()
            .subscribeOn(Schedulers.io())
            .toSingleDefault(Result.success())
            .onErrorReturn { error ->
                Timber.tag(LOG_TAG).e(error)
                if (error.isRetryable) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
            .doOnSuccess { Timber.tag(LOG_TAG).d("Finished with result %s", it) }
    }

    companion object {
        private const val LOG_TAG = "DeleteMessagesWorker"
    }
}
