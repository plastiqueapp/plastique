package io.plastique.test

import com.squareup.rx2.idler.Rx2Idler
import io.plastique.core.init.Initializer
import io.reactivex.plugins.RxJavaPlugins
import javax.inject.Inject

class RxIdlerInitializer @Inject constructor() : Initializer() {
    override fun initialize() {
        RxJavaPlugins.setInitComputationSchedulerHandler(Rx2Idler.create("Idling Computation Scheduler"))
        RxJavaPlugins.setInitIoSchedulerHandler(Rx2Idler.create("Idling I/O Scheduler"))
        RxJavaPlugins.setInitSingleSchedulerHandler(Rx2Idler.create("Idling Single Scheduler"))
        RxJavaPlugins.setInitNewThreadSchedulerHandler(Rx2Idler.create("Idling New Thread Scheduler"))
    }

    @Suppress("MagicNumber")
    override val priority: Int
        get() = 9
}
