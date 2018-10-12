package io.plastique.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import io.plastique.core.ExpandableToolbarLayout
import io.plastique.core.MvvmFragment
import io.plastique.core.extensions.isVisible
import io.plastique.core.navigation.navigationContext
import io.plastique.inject.getComponent
import io.plastique.main.MainPage
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class ProfileFragment : MvvmFragment<ProfileViewModel>(), MainPage {
    private lateinit var loginButton: Button
    @Inject lateinit var navigator: ProfileNavigator

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loginButton = view.findViewById(R.id.button_login)
        loginButton.setOnClickListener { navigator.openLogin(navigationContext) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeState()
    }

    private fun observeState() {
        viewModel.state
                .map { state -> state.showLoginButton }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { show -> loginButton.isVisible = show }
                .disposeOnDestroy()
    }

    override fun getTitle(): Int = R.string.profile_title

    override fun createAppBarViews(parent: ExpandableToolbarLayout) {
    }

    override fun injectDependencies() {
        getComponent<ProfileFragmentComponent>().inject(this)
    }
}
