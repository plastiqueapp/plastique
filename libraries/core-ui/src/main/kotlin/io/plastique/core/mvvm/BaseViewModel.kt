package io.plastique.core.mvvm

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import io.plastique.core.DisposableContainer
import io.plastique.core.DisposableContainerImpl
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

abstract class BaseViewModel : ViewModel(), DisposableContainer by DisposableContainerImpl() {
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

    override fun onCleared() {
        super.onCleared()
        disposeAll()
    }
}
