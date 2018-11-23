package io.plastique.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.isVisible
import io.plastique.core.ExpandableToolbarLayout
import io.plastique.core.MvvmFragment
import io.plastique.core.navigation.navigationContext
import io.plastique.inject.getComponent
import io.plastique.main.MainPage
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class ProfileFragment : MvvmFragment<ProfileViewModel>(), MainPage {
    private lateinit var signInButton: Button
    @Inject lateinit var navigator: ProfileNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        signInButton = view.findViewById(R.id.button_sign_in)
        signInButton.setOnClickListener { navigator.openLogin(navigationContext) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.state
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { renderState(it) }
                .disposeOnDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_profile, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.profile_action_view_watchers -> {
            navigator.openWatchers(navigationContext, null)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun renderState(state: ProfileViewState) {
        signInButton.isVisible = state.showSignInButton
    }

    override fun getTitle(): Int = R.string.profile_title

    override fun createAppBarViews(parent: ExpandableToolbarLayout) {
    }

    override fun injectDependencies() {
        getComponent<ProfileFragmentComponent>().inject(this)
    }
}
