package io.plastique.users.profile.about

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.plastique.core.BaseFragment
import io.plastique.core.ScrollableToTop
import io.plastique.core.mvvm.viewModel
import io.plastique.core.navigation.navigationContext
import io.plastique.inject.getComponent
import io.plastique.users.UsersFragmentComponent
import io.plastique.users.databinding.FragmentUsersAboutBinding
import io.plastique.users.profile.about.AboutEvent.RetryClickEvent
import io.reactivex.android.schedulers.AndroidSchedulers

class AboutFragment : BaseFragment(), ScrollableToTop {
    private val viewModel: AboutViewModel by viewModel()
    private lateinit var view: AboutView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.navigator.attach(navigationContext)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentUsersAboutBinding.inflate(inflater, container, false)
        val view = AboutView(
            this,
            binding,
            onRetryClick = { viewModel.dispatch(RetryClickEvent) })

        val username = requireArguments().getString(ARG_USERNAME)!!
        viewModel.init(username)
        viewModel.state
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { view.render(it) }
            .disposeOnDestroy()

        return binding.root
    }

    override fun scrollToTop() {
        view.scrollToTop()
    }

    override fun injectDependencies() {
        getComponent<UsersFragmentComponent>().inject(this)
    }

    companion object {
        private const val ARG_USERNAME = "username"

        fun newArgs(username: String): Bundle {
            return Bundle().apply {
                putString(ARG_USERNAME, username)
            }
        }
    }
}
