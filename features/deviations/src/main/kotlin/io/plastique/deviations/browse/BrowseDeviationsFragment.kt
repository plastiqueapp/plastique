package io.plastique.deviations.browse

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
import com.github.technoir42.android.extensions.doOnTabReselected
import com.github.technoir42.android.extensions.layoutInflater
import io.plastique.core.BaseFragment
import io.plastique.core.ExpandableToolbarLayout
import io.plastique.core.ScrollableToTop
import io.plastique.core.mvvm.viewModel
import io.plastique.core.pager.FragmentListPagerAdapter
import io.plastique.deviations.DeviationsFragmentComponent
import io.plastique.deviations.R
import io.plastique.deviations.databinding.FragmentBrowseDeviationsBinding
import io.plastique.deviations.databinding.IncBrowseAppbarBinding
import io.plastique.deviations.list.DailyDeviationsFragment
import io.plastique.deviations.list.HotDeviationsFragment
import io.plastique.deviations.list.LayoutMode
import io.plastique.deviations.list.PopularDeviationsFragment
import io.plastique.deviations.list.UndiscoveredDeviationsFragment
import io.plastique.deviations.tags.TagManager
import io.plastique.deviations.tags.TagManagerProvider
import io.plastique.inject.getComponent
import io.plastique.main.MainPage

class BrowseDeviationsFragment : BaseFragment(),
    MainPage,
    ScrollableToTop,
    TagManagerProvider {

    private val viewModel: BrowseDeviationsViewModel by viewModel()

    private lateinit var binding: FragmentBrowseDeviationsBinding
    private lateinit var appbarBinding: IncBrowseAppbarBinding
    private lateinit var expandableToolbarLayout: ExpandableToolbarLayout
    private lateinit var pagerAdapter: FragmentListPagerAdapter
    private var switchLayoutMenuItem: MenuItem? = null
    private var layoutMode: LayoutMode = LayoutMode.DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBrowseDeviationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pagerAdapter = FragmentListPagerAdapter(this, PAGES)
        binding.pager.adapter = pagerAdapter
        binding.pager.pageMargin = resources.getDimensionPixelOffset(R.dimen.deviations_browse_page_spacing)

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

        appbarBinding = IncBrowseAppbarBinding.inflate(parent.layoutInflater, parent)
        appbarBinding.tabs.setupWithViewPager(binding.pager)
        appbarBinding.tabs.doOnTabReselected { tab ->
            val fragment = pagerAdapter.getFragment(tab.position)
            if (fragment is ScrollableToTop) {
                fragment.scrollToTop()
            }
        }
    }

    override fun scrollToTop() {
        val currentFragment = pagerAdapter.getFragment(binding.pager.currentItem)
        if (currentFragment is ScrollableToTop) {
            currentFragment.scrollToTop()
        }
    }

    override val tagManager: TagManager get() = appbarBinding.tags

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
