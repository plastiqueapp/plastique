package io.plastique.core.mvvm

import android.os.Bundle
import io.plastique.core.BaseActivity
import javax.inject.Inject

abstract class MvvmActivity<VM : BaseViewModel> : BaseActivity() {
    @Inject protected lateinit var viewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.subscribeToLifecycle(lifecycle)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            viewModel.destroy()
        }
    }
}
