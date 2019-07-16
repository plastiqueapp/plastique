package io.plastique.deviations

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import androidx.viewpager.widget.ViewPager
import com.github.technoir42.android.extensions.doOnTabReselected
import com.google.android.material.tabs.TabLayout
import io.plastique.core.ExpandableToolbarLayout
import io.plastique.core.ScrollableToTop
import io.plastique.core.mvvm.MvvmFragment
import io.plastique.core.pager.FragmentListPagerAdapter
import io.plastique.deviations.list.DailyDeviationsFragment
import io.plastique.deviations.list.HotDeviationsFragment
import io.plastique.deviations.list.LayoutMode
import io.plastique.deviations.list.PopularDeviationsFragment
import io.plastique.deviations.list.UndiscoveredDeviationsFragment
import io.plastique.deviations.tags.TagManager
import io.plastique.deviations.tags.TagManagerProvider
import io.plastique.deviations.tags.TagsView
import io.plastique.inject.getComponent
import io.plastique.main.MainPage

class BrowseDeviationsFragment : MvvmFragment<BrowseDeviationsViewModel>(BrowseDeviationsViewModel::class.java),
    MainPage,
    ScrollableToTop,
    TagManagerProvider {

    private lateinit var expandableToolbarLayout: ExpandableToolbarLayout
    private lateinit var tagsView: TagsView
    private lateinit var pager: ViewPager
    private lateinit var pagerAdapter: FragmentListPagerAdapter
    private var switchLayoutMenuItem: MenuItem? = null
    private var layoutMode: LayoutMode = LayoutMode.DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_browse_deviations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pagerAdapter = FragmentListPagerAdapter(this, PAGES)
        pager = view.findViewById(R.id.pager)
        pager.adapter = pagerAdapter
        pager.pageMargin = resources.getDimensionPixelOffset(R.dimen.deviations_browse_page_spacing)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.layoutMode
            .subscribe { layoutMode ->
                this.layoutMode = layoutMode
                switchLayoutMenuItem?.setIcon(getLayoutModeIconId(layoutMode))
            }
            .disposeOnDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_browse_deviations, menu)

        val filterItem = menu.findItem(R.id.deviations_action_filters)
        filterItem.setIcon(getFilterIconId(expandableToolbarLayout.isExpanded))

        switchLayoutMenuItem = menu.findItem(R.id.deviations_action_switch_layout)
        switchLayoutMenuItem!!.setIcon(getLayoutModeIconId(layoutMode))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.deviations_action_filters -> {
            expandableToolbarLayout.isExpanded = !expandableToolbarLayout.isExpanded
            animateFilterIcon(item)
            true
        }
        R.id.deviations_action_switch_layout_grid -> {
            viewModel.setLayoutMode(LayoutMode.Grid)
            true
        }
        R.id.deviations_action_switch_layout_flex -> {
            viewModel.setLayoutMode(LayoutMode.Flex)
            true
        }
        R.id.deviations_action_switch_layout_list -> {
            viewModel.setLayoutMode(LayoutMode.List)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun getTitle(): Int = R.string.deviations_browse_title

    override fun createAppBarViews(parent: ExpandableToolbarLayout) {
        expandableToolbarLayout = parent

        View.inflate(parent.context, R.layout.inc_browse_appbar, parent)

        val tabLayout: TabLayout = parent.findViewById(R.id.browse_tabs)
        tabLayout.setupWithViewPager(pager)
        tabLayout.doOnTabReselected { tab ->
            val fragment = pagerAdapter.getFragment(tab.position)
            if (fragment is ScrollableToTop) {
                fragment.scrollToTop()
            }
        }

        tagsView = parent.findViewById(R.id.browse_tags)
    }

    override fun scrollToTop() {
        val currentFragment = pagerAdapter.getFragment(pager.currentItem)
        if (currentFragment is ScrollableToTop) {
            currentFragment.scrollToTop()
        }
    }

    override val tagManager: TagManager get() = tagsView

    override fun injectDependencies() {
        getComponent<DeviationsFragmentComponent>().inject(this)
    }

    private fun animateFilterIcon(item: MenuItem) {
        if (item.icon is Animatable) {
            item.icon.mutate()
            (item.icon as Animatable).start()

            val nextIconId = getFilterIconId(expandableToolbarLayout.isExpanded)
            AnimatedVectorDrawableCompat.clearAnimationCallbacks(item.icon)
            AnimatedVectorDrawableCompat.registerAnimationCallback(item.icon, object : Animatable2Compat.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable) {
                    AnimatedVectorDrawableCompat.unregisterAnimationCallback(drawable, this)
                    item.setIcon(nextIconId)
                }
            })
        }
    }

    @DrawableRes
    private fun getFilterIconId(expanded: Boolean): Int = when {
        expanded -> R.drawable.ic_filters_collapse_24dp
        else -> R.drawable.ic_filters_expand_24dp
    }

    @DrawableRes
    private fun getLayoutModeIconId(layoutMode: LayoutMode): Int = when (layoutMode) {
        LayoutMode.Grid -> R.drawable.ic_layout_grid_24dp
        LayoutMode.Flex -> R.drawable.ic_layout_flex_24dp
        LayoutMode.List -> R.drawable.ic_layout_list_24dp
    }

    companion object {
        private val PAGES = listOf(
            FragmentListPagerAdapter.Page(R.string.deviations_browse_page_hot, HotDeviationsFragment::class.java),
            FragmentListPagerAdapter.Page(R.string.deviations_browse_page_popular, PopularDeviationsFragment::class.java),
            FragmentListPagerAdapter.Page(R.string.deviations_browse_page_undiscovered, UndiscoveredDeviationsFragment::class.java),
            FragmentListPagerAdapter.Page(R.string.deviations_browse_page_daily, DailyDeviationsFragment::class.java))
    }
}
