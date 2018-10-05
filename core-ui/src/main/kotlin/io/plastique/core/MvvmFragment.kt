package io.plastique.core

import android.os.Bundle
import io.plastique.core.extensions.isRemovingSelfOrParent
import javax.inject.Inject

abstract class MvvmFragment<VM : ViewModel> : BaseFragment() {
    @Inject protected lateinit var viewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.subscribeToLifecycle(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (requireActivity().isFinishing || isRemovingSelfOrParent) {
            viewModel.destroy()
        }
    }
}
