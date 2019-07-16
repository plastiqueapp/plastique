package io.plastique.core.mvvm

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import io.plastique.core.BaseFragment
import io.plastique.core.pager.isInViewPager

abstract class MvvmFragment<VM : BaseViewModel>(private val viewModelClass: Class<VM>) : BaseFragment() {
    protected val viewModel: VM by lazy(LazyThreadSafetyMode.NONE) {
        if (isInViewPager) {
            val viewModelStore = parentFragment?.viewModelStore ?: requireActivity().viewModelStore
            ViewModelProvider(viewModelStore, fragmentComponent.viewModelFactory()).get(javaClass.name, viewModelClass)
        } else {
            ViewModelProviders.of(this, fragmentComponent.viewModelFactory()).get(viewModelClass)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.subscribeToLifecycle(lifecycle)
    }
}
