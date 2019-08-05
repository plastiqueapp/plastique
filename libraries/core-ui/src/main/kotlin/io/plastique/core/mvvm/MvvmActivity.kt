package io.plastique.core.mvvm

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import io.plastique.core.BaseActivity

abstract class MvvmActivity<VM : BaseViewModel>(private val viewModelClass: Class<VM>) : BaseActivity() {
    protected val viewModel: VM by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProviders.of(this, activityComponent.viewModelFactory()).get(viewModelClass)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.subscribeToLifecycle(lifecycle)
    }
}
