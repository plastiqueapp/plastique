package io.plastique

import android.app.Application
import android.os.Looper
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.jakewharton.threetenabp.AndroidThreeTen
import com.squareup.leakcanary.LeakCanary
import io.fabric.sdk.android.Fabric
import io.plastique.core.analytics.Analytics
import io.plastique.inject.ActivityComponent
import io.plastique.inject.AppComponent
import io.plastique.inject.components.ModuleAppComponent
import io.plastique.inject.getComponent
import io.plastique.util.CrashlyticsTree
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber
import javax.inject.Inject

abstract class BasePlastiqueApplication : Application(), AppComponent.Holder, ActivityComponent.Factory {
    @Inject lateinit var analytics: Analytics
    @Inject lateinit var workerFactory: WorkerFactory

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }
        init()
    }

    protected open fun init() {
        AndroidThreeTen.init(this)

        @Suppress("ConstantConditionIf")
        if (BuildConfig.GOOGLE_SERVICES_ENABLED) {
            initFabric()
            Timber.plant(CrashlyticsTree())
        }

        injectDependencies()
        initRxJava()
        initWorkManager()

        analytics.initUserProperties()
    }

    private fun initFabric() {
        val crashlytics = Crashlytics.Builder()
                .core(CrashlyticsCore.Builder()
                        .disabled(BuildConfig.DEBUG)
                        .build())
                .build()
        Fabric.with(Fabric.Builder(this)
                .kits(crashlytics)
                .debuggable(BuildConfig.DEBUG)
                .build())
    }

    private fun initRxJava() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler {
            AndroidSchedulers.from(Looper.getMainLooper(), true)
        }

        RxJavaPlugins.setErrorHandler { error ->
            if (error !is UndeliverableException) {
                Timber.e(error)
            }
        }
    }

    private fun initWorkManager() {
        WorkManager.initialize(this, Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build())
    }

    private fun injectDependencies() {
        getComponent<ModuleAppComponent>().inject(this)
    }

    override fun createActivityComponent(): ActivityComponent {
        return appComponent.createActivityComponent()
    }
}
