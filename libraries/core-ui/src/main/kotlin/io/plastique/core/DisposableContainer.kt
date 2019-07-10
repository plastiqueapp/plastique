package io.plastique.core

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

interface DisposableContainer {
    fun disposeAll()

    fun <T : Disposable> T.disposeOnDestroy(): T
}

class DisposableContainerImpl : DisposableContainer {
    private val disposables = CompositeDisposable()

    override fun disposeAll() {
        disposables.dispose()
    }

    override fun <T : Disposable> T.disposeOnDestroy(): T {
        disposables.add(this)
        return this
    }
}
