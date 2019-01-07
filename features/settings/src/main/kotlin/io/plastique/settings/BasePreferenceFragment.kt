package io.plastique.settings

import android.content.Context
import androidx.preference.PreferenceFragmentCompat
import io.plastique.inject.ActivityComponent
import io.plastique.inject.FragmentComponent
import io.plastique.inject.getComponent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BasePreferenceFragment : PreferenceFragmentCompat(), FragmentComponent.Holder {
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

    override val fragmentComponent: FragmentComponent by lazy(LazyThreadSafetyMode.NONE) {
        requireActivity().getComponent<ActivityComponent>().createFragmentComponent()
    }
}
