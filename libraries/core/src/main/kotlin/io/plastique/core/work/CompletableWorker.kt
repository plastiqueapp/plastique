package io.plastique.core.work

import android.content.Context
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import io.plastique.core.exceptions.isRetryable
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber

abstract class CompletableWorker(appContext: Context, workerParams: WorkerParameters) : RxWorker(appContext, workerParams) {
    abstract fun createCompletableWork(): Completable

    override fun createWork(): Single<Result> {
        Timber.tag(logTag).d("Started")
        return createCompletableWork()
            .toSingleDefault(Result.success())
            .onErrorReturn { error ->
                Timber.tag(logTag).e(error)
                if (error.isRetryable) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
            .doOnSuccess { Timber.tag(logTag).d("Finished with result %s", it) }
    }

    private val logTag: String get() = javaClass.simpleName
}
