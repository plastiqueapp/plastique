package io.plastique.core.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class DaggerViewModelFactory @Inject constructor(
    private val specificFactories: Map<Class<*>, @JvmSuppressWildcards Provider<ViewModel>>
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val factory = specificFactories[modelClass]
            ?: throw IllegalStateException("No provider found for $modelClass")
        @Suppress("UNCHECKED_CAST")
        return factory.get() as T
    }
}
