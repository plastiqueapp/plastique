package io.plastique.test

import io.reactivex.Scheduler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.disposables.Disposable
import io.reactivex.internal.schedulers.ExecutorScheduler
import io.reactivex.plugins.RxJavaPlugins
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class RxSchedulersOverrideExtension : BeforeEachCallback, AfterEachCallback {
    override fun beforeEach(context: ExtensionContext) {
        RxJavaPlugins.setInitIoSchedulerHandler { ImmediateScheduler }
        RxJavaPlugins.setInitComputationSchedulerHandler { ImmediateScheduler }
        RxJavaPlugins.setInitSingleSchedulerHandler { ImmediateScheduler }
        RxJavaPlugins.setInitNewThreadSchedulerHandler { ImmediateScheduler }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { ImmediateScheduler }
    }

    override fun afterEach(context: ExtensionContext) {
        RxJavaPlugins.reset()
        RxAndroidPlugins.reset()
    }

    private object ImmediateScheduler : Scheduler() {
        override fun scheduleDirect(run: Runnable, delay: Long, unit: TimeUnit): Disposable {
            return super.scheduleDirect(run, 0, unit)
        }

        override fun createWorker(): Scheduler.Worker {
            return ExecutorScheduler.ExecutorWorker(Executor { it.run() }, false)
        }
    }
}
