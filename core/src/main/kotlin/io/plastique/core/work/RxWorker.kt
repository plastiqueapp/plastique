package io.plastique.core.work

import android.content.Context
import androidx.concurrent.futures.AbstractResolvableFuture
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.DisposableHelper
import java.util.concurrent.atomic.AtomicReference

abstract class RxWorker(context: Context, workParams: WorkerParameters) : ListenableWorker(context, workParams) {
    abstract fun doWork(): Single<Result>

    final override fun startWork(): ListenableFuture<Result> {
        return doWork().toListenableFuture()
    }
}

internal fun <T> Single<T>.toListenableFuture(): ListenableFuture<T> {
    return subscribeWith(ListenableFutureObserver())
}

private class ListenableFutureObserver<T> : AbstractResolvableFuture<T>(), SingleObserver<T> {
    private val upstream = AtomicReference<Disposable>()

    override fun onSubscribe(d: Disposable) {
        DisposableHelper.setOnce(upstream, d)
    }

    override fun onSuccess(value: T) {
        set(value)
    }

    override fun onError(e: Throwable) {
        setException(e)
    }

    override fun afterDone() {
        DisposableHelper.dispose(upstream)
    }
}
