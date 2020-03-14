package io.plastique.main

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import com.github.technoir42.android.extensions.disableDragging
import com.github.technoir42.android.extensions.setActionBar
import com.github.technoir42.android.extensions.setTitleOnClickListener
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.plastique.core.BaseActivity
import io.plastique.core.ExpandableToolbarLayout
import io.plastique.core.ScrollableToTop
import io.plastique.core.image.ImageLoader
import io.plastique.core.image.TransformType
import io.plastique.core.mvvm.viewModel
import io.plastique.core.navigation.navigationContext
import io.plastique.inject.getComponent
import io.plastique.users.User
import io.plastique.util.InstantAppHelper
import io.plastique.util.Size
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class MainActivity : BaseActivity(R.layout.activity_main),
    BottomNavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemReselectedListener {

    @Inject lateinit var mainPageProvider: MainPageProvider
    @Inject lateinit var navigator: MainNavigator
    @Inject lateinit var instantAppHelper: InstantAppHelper

    private val imageLoader = ImageLoader.from(this)
    private val viewModel: MainViewModel by viewModel()

    private lateinit var expandableToolbarLayout: ExpandableToolbarLayout
    private var currentUser: User? = null

    private val fragmentLifecycleCallbacks = object : FragmentLifecycleCallbacks() {
        override fun onFragmentViewCreated(fragmentManager: FragmentManager, fragment: Fragment, view: View, savedInstanceState: Bundle?) {
            if (fragment is MainPage) {
                onPageCreated(fragment)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigator.attach(navigationContext)

        val toolbar = setActionBar(R.id.toolbar)
        toolbar.setTitleOnClickListener(View.OnClickListener { scrollToTop() })

        val appBar = findViewById<AppBarLayout>(R.id.appbar)
        appBar.disableDragging()
        expandableToolbarLayout = findViewById(R.id.expandable_toolbar)

        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener(this)
        bottomNavigationView.setOnNavigationItemReselectedListener(this)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.tab_content, mainPageProvider.getPageFragmentClass(R.id.main_tab_browse), null)
                .commit()
        }

        viewModel.init()
        viewModel.state
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { state -> renderState(state) }
            .disposeOnDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)

        val installFullMenuItem = menu.findItem(R.id.main_action_install_full_version)
        installFullMenuItem.isVisible = instantAppHelper.isInstantApp
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            currentUser?.let { navigator.openUserProfile(it) }
            true
        }
        R.id.main_action_settings -> {
            navigator.openSettings()
            true
        }
        R.id.main_action_install_full_version -> {
            instantAppHelper.showInstallPrompt(this)
            true
        }
        else -> false
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.tab_content, mainPageProvider.getPageFragmentClass(item.itemId), null)
            .commit()
        return true
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        scrollToTop()
    }

    private fun renderState(state: MainViewState) {
        currentUser = state.user

        if (state.user != null) {
            val avatarSize = resources.getDimensionPixelSize(R.dimen.common_avatar_size_small)
            imageLoader.load(state.user.avatarUrl)
                .params {
                    size = Size(avatarSize, avatarSize)
                    placeholderDrawable = R.drawable.default_avatar_32dp
                    errorDrawable = R.drawable.default_avatar_32dp
                    transforms += TransformType.CircleCrop
                }
                .enqueue { setCurrentUserIcon(it) }
        } else {
            setCurrentUserIcon(null)
        }
    }

    private fun scrollToTop() {
        val fragment = supportFragmentManager.findFragmentById(R.id.tab_content)
        if (fragment is ScrollableToTop) {
            fragment.scrollToTop()
        }
    }

    override fun injectDependencies() {
        getComponent<MainActivityComponent>().inject(this)
    }

    private fun onPageCreated(page: MainPage) {
        supportActionBar?.setTitle(page.getTitle())
        if (expandableToolbarLayout.childCount > 1) {
            expandableToolbarLayout.removeViews(1, expandableToolbarLayout.childCount - 1)
        }
        page.createAppBarViews(expandableToolbarLayout)
    }

    private fun setCurrentUserIcon(drawable: Drawable?) {
        supportActionBar!!.apply {
            setDisplayHomeAsUpEnabled(drawable != null)
            setHomeAsUpIndicator(drawable)
        }
    }
}
