package io.plastique.core.mvvm

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject

abstract class BaseViewModel {
    private val disposables = CompositeDisposable()
    private val _screenVisible: BehaviorSubject<Boolean> = BehaviorSubject.create()
    protected val screenVisible: Observable<Boolean> get() = _screenVisible

    fun subscribeToLifecycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                _screenVisible.onNext(true)
            }

            override fun onStop(owner: LifecycleOwner) {
                _screenVisible.onNext(false)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                lifecycle.removeObserver(this)
            }
        })
    }

    fun destroy() {
        disposables.dispose()
    }

    protected fun <T : Disposable> T.disposeOnDestroy(): T {
        disposables.add(this)
        return this
    }
}
