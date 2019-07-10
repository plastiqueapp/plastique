package io.plastique.core

import android.content.Context
import androidx.fragment.app.Fragment
import io.plastique.inject.BaseActivityComponent
import io.plastique.inject.BaseFragmentComponent
import io.plastique.inject.getComponent

abstract class BaseFragment :
    Fragment(),
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
}
