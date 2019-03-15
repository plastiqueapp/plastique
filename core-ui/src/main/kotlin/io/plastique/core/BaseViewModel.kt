package io.plastique.core

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.sch.rxjava2.extensions.Transformers
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject

abstract class BaseViewModel {
    private val disposables = CompositeDisposable()
    private val started = BehaviorSubject.create<Boolean>()

    fun subscribeToLifecycle(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                started.onNext(true)
            }

            override fun onStop(owner: LifecycleOwner) {
                started.onNext(false)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                owner.lifecycle.removeObserver(this)
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

    protected fun <T> Observable<out T>.bindToLifecycle(): Observable<T> {
        return compose(Transformers.valveLast(started, true))
                .distinctUntilChanged()
    }
}
