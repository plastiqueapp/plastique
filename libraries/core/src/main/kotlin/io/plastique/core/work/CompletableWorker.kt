package io.plastique.core.work

import android.content.Context
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import io.plastique.core.client.HttpException
import io.plastique.core.client.HttpResponseCodes
import io.plastique.core.client.HttpTransportException
import io.plastique.core.client.NoNetworkConnectionException
import io.plastique.core.client.RateLimitExceededException
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

    private val Throwable.isRetryable: Boolean
        get() = when (this) {
            is NoNetworkConnectionException,
            is HttpTransportException,
            is RateLimitExceededException -> true
            is HttpException -> responseCode >= HttpResponseCodes.INTERNAL_SERVER_ERROR
            else -> false
        }

    private val logTag: String get() = javaClass.simpleName
}
