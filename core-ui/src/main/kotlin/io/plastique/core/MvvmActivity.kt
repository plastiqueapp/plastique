package io.plastique.core

import android.os.Bundle
import javax.inject.Inject

abstract class MvvmActivity<VM : ViewModel> : BaseActivity() {
    @Inject protected lateinit var viewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.subscribeToLifecycle(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            viewModel.destroy()
        }
    }
}
