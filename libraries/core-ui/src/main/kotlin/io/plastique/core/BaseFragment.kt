package io.plastique.core

import android.content.Context
import androidx.fragment.app.Fragment
import io.plastique.inject.BaseActivityComponent
import io.plastique.inject.BaseFragmentComponent
import io.plastique.inject.getComponent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseFragment : Fragment(), BaseFragmentComponent.Holder {
    private val disposables = CompositeDisposable()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        injectDependencies()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.dispose()
    }

    protected fun <T : Disposable> T.disposeOnDestroy(): T {
        disposables.add(this)
        return this
    }

    protected abstract fun injectDependencies()

    override val fragmentComponent: BaseFragmentComponent by lazy(LazyThreadSafetyMode.NONE) {
        requireActivity().getComponent<BaseActivityComponent>().createFragmentComponent()
    }
}
