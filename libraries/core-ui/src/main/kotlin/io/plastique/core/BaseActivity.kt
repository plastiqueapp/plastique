package io.plastique.core

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import io.plastique.core.config.AppConfig
import io.plastique.inject.BaseActivityComponent
import io.plastique.inject.BaseAppComponent
import io.plastique.inject.BaseFragmentComponent
import io.plastique.inject.getComponent
import javax.inject.Inject

abstract class BaseActivity(@LayoutRes contentLayoutId: Int) : AppCompatActivity(contentLayoutId),
    BaseActivityComponent.Holder,
    BaseFragmentComponent.Factory,
    DisposableContainer by DisposableContainerImpl() {

    @Inject protected lateinit var appConfig: AppConfig

    private var hasMenu: Boolean = true
    protected var optionsMenu: Menu? = null

    protected abstract fun injectDependencies()

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        appConfig.fetch()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposeAll()
    }

    final override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (hasMenu) {
            optionsMenu = menu
            onCreateOptionsMenu(menu, menuInflater)
        } else {
            optionsMenu = null
        }
        return super.onCreateOptionsMenu(menu)
    }

    open fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    fun setHasOptionsMenu(hasMenu: Boolean) {
        if (this.hasMenu != hasMenu) {
            this.hasMenu = hasMenu
            invalidateOptionsMenu()
        }
    }

    override val activityComponent: BaseActivityComponent by lazy(LazyThreadSafetyMode.NONE) {
        @Suppress("DEPRECATION")
        lastCustomNonConfigurationInstance as BaseActivityComponent?
            ?: application.getComponent<BaseAppComponent>().createActivityComponent()
    }

    override fun createFragmentComponent(): BaseFragmentComponent {
        return activityComponent.createFragmentComponent()
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory = activityComponent.viewModelFactory()

    @Suppress("OverridingDeprecatedMember")
    override fun onRetainCustomNonConfigurationInstance(): Any = activityComponent
}
