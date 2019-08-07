package io.plastique.settings

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceFragmentCompat
import io.plastique.core.DisposableContainer
import io.plastique.core.DisposableContainerImpl
import io.plastique.inject.BaseActivityComponent
import io.plastique.inject.BaseFragmentComponent
import io.plastique.inject.getComponent

abstract class BasePreferenceFragment :
    PreferenceFragmentCompat(),
    BaseFragmentComponent.Holder,
    DisposableContainer by DisposableContainerImpl() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        injectDependencies()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposeAll()
    }

    protected abstract fun injectDependencies()

    override val fragmentComponent: BaseFragmentComponent by lazy(LazyThreadSafetyMode.NONE) {
        requireActivity().getComponent<BaseActivityComponent>().createFragmentComponent()
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory = fragmentComponent.viewModelFactory()
}
