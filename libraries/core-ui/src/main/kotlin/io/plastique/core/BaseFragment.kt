package io.plastique.core

import android.content.Context
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.plastique.inject.BaseActivityComponent
import io.plastique.inject.BaseFragmentComponent
import io.plastique.inject.getComponent

abstract class BaseFragment(@LayoutRes contentLayoutId: Int) :
    Fragment(contentLayoutId),
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
