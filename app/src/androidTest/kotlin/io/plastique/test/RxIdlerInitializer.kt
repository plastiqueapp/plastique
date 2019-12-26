package io.plastique.test

import com.squareup.rx2.idler.IdlingResourceScheduler
import com.squareup.rx2.idler.Rx2Idler
import io.plastique.core.init.Initializer
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class RxIdlerInitializer @Inject constructor() : Initializer() {
    override fun initialize() {
        RxJavaPlugins.setInitComputationSchedulerHandler(Rx2Idler.create("Idling Computation Scheduler"))
        RxJavaPlugins.setInitIoSchedulerHandler(Rx2Idler.create("Idling I/O Scheduler"))
        RxJavaPlugins.setInitSingleSchedulerHandler(Rx2Idler.create("Idling Single Scheduler"))
        RxJavaPlugins.setInitNewThreadSchedulerHandler(Rx2Idler.create("Idling New Thread Scheduler"))

        check(Schedulers.computation() is IdlingResourceScheduler) { "Computation scheduler is already initialized" }
        check(Schedulers.io() is IdlingResourceScheduler) { "IO scheduler is already initialized" }
        check(Schedulers.single() is IdlingResourceScheduler) { "Single scheduler is already initialized" }
        check(Schedulers.newThread() is IdlingResourceScheduler) { "New thread scheduler is already initialized" }
    }

    @Suppress("MagicNumber")
    override val priority: Int
        get() = 9
}
