package io.plastique.core.mvvm

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.plastique.core.pager.isInViewPager
import io.plastique.inject.BaseActivityComponent
import io.plastique.inject.BaseFragmentComponent
import io.plastique.inject.getComponent

inline fun <reified VM : BaseViewModel> ComponentActivity.viewModel(): Lazy<VM> = viewModel(VM::class.java)

inline fun <reified VM : BaseViewModel> Fragment.viewModel(): Lazy<VM> = viewModel(VM::class.java)

@PublishedApi
internal fun <VM : BaseViewModel> ComponentActivity.viewModel(viewModelClass: Class<VM>): Lazy<VM> =
    lazy(LazyThreadSafetyMode.NONE) {
        val viewModelFactory = getComponent<BaseActivityComponent>().viewModelFactory()
        ViewModelProvider(this, viewModelFactory).get(viewModelClass).also { viewModel ->
            viewModel.subscribeToLifecycle(lifecycle)
        }
    }

@PublishedApi
internal fun <VM : BaseViewModel> Fragment.viewModel(viewModelClass: Class<VM>): Lazy<VM> =
    lazy(LazyThreadSafetyMode.NONE) {
        val owner = if (isInViewPager) parentFragment ?: requireActivity() else this
        val viewModelFactory = getComponent<BaseFragmentComponent>().viewModelFactory()
        ViewModelProvider(owner, viewModelFactory).get(javaClass.name, viewModelClass).also { viewModel ->
            viewModel.subscribeToLifecycle(lifecycle)
        }
    }
