package io.plastique.core.mvvm

import android.os.Bundle
import io.plastique.core.BaseFragment
import io.plastique.core.extensions.isRemovingSelfOrParent
import javax.inject.Inject

abstract class MvvmFragment<VM : BaseViewModel> : BaseFragment() {
    @Inject protected lateinit var viewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.subscribeToLifecycle(lifecycle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (requireActivity().isFinishing || isRemovingSelfOrParent) {
            viewModel.destroy()
        }
    }
}
