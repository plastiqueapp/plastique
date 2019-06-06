package io.plastique.core.init

import android.os.Looper
import com.github.technoir42.rxjava2.extensions.FailFastErrorHandler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.plugins.RxJavaPlugins
import javax.inject.Inject

class RxJavaInitializer @Inject constructor() : Initializer() {
    override fun initialize() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler {
            AndroidSchedulers.from(Looper.getMainLooper(), true)
        }

        RxJavaPlugins.setErrorHandler(FailFastErrorHandler())
    }
}
