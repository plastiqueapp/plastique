package io.plastique.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.plastique.collections.CollectionsFragment
import io.plastique.core.ExpandableToolbarLayout
import io.plastique.core.MvvmActivity
import io.plastique.core.ScrollableToTop
import io.plastique.core.extensions.getLayoutBehavior
import io.plastique.core.extensions.setActionBar
import io.plastique.core.navigation.navigationContext
import io.plastique.deviations.BrowseDeviationsFragment
import io.plastique.gallery.GalleryFragment
import io.plastique.inject.getComponent
import io.plastique.notifications.NotificationsFragment
import io.plastique.profile.ProfileFragment
import io.plastique.util.DragDisabledCallback
import javax.inject.Inject

class MainActivity : MvvmActivity<MainViewModel>(), BottomNavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemReselectedListener {
    private lateinit var expandableToolbarLayout: ExpandableToolbarLayout
    @Inject lateinit var navigator: MainNavigator

    private val fragmentLifecycleCallbacks = object : FragmentLifecycleCallbacks() {
        override fun onFragmentViewCreated(fm: FragmentManager, fragment: Fragment, view: View, savedInstanceState: Bundle?) {
            if (fragment is MainPage) {
                onPageCreated(fragment)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setActionBar(R.id.toolbar)

        expandableToolbarLayout = findViewById(R.id.expandable_toolbar)

        val appBar = findViewById<AppBarLayout>(R.id.appbar)
        val appBarBehavior = appBar.getLayoutBehavior<AppBarLayout.Behavior>()
        appBarBehavior.setDragCallback(DragDisabledCallback)

        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener(this)
        bottomNavigationView.setOnNavigationItemReselectedListener(this)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.tab_content, BrowseDeviationsFragment())
                    .commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val logoutItem = menu.findItem(R.id.main_action_logout)
        logoutItem.isVisible = viewModel.isLoggedIn()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.main_action_settings -> {
            navigator.openSettings(navigationContext)
            true
        }
        R.id.main_action_logout -> {
            viewModel.onLogoutClick()
            true
        }
        else -> false
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val ft = supportFragmentManager.beginTransaction()
        when (item.itemId) {
            R.id.main_tab_browse -> ft.replace(R.id.tab_content, BrowseDeviationsFragment())
            R.id.main_tab_collections -> ft.replace(R.id.tab_content, CollectionsFragment.newInstance())
            R.id.main_tab_profile -> ft.replace(R.id.tab_content, ProfileFragment())
            R.id.main_tab_gallery -> ft.replace(R.id.tab_content, GalleryFragment.newInstance())
            R.id.main_tab_notifications -> ft.replace(R.id.tab_content, NotificationsFragment())
        }
        ft.commit()
        return true
    }

    override fun onNavigationItemReselected(item: MenuItem) {
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
}
