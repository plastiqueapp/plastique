package io.plastique.core.mvvm

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import io.plastique.core.BaseActivity
import javax.inject.Inject

abstract class MvvmActivity<VM : BaseViewModel>(private val viewModelClass: Class<VM>) : BaseActivity() {
    @Inject protected lateinit var viewModelFactory: ViewModelProvider.Factory

    protected val viewModel: VM by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProviders.of(this, activityComponent.viewModelFactory()).get(viewModelClass)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.subscribeToLifecycle(lifecycle)
    }
}
