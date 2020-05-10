package io.plastique.auth

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import io.plastique.auth.LoginEvent.ErrorDialogClosedEvent
import io.plastique.core.BaseActivity
import io.plastique.core.dialogs.OnCancelDialogListener
import io.plastique.core.mvvm.viewModel
import io.plastique.core.navigation.Route
import io.plastique.core.navigation.activityRoute
import io.plastique.core.navigation.navigationContext
import io.plastique.inject.getComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class LoginActivity : BaseActivity(), OnCancelDialogListener {
    private val viewModel: LoginViewModel by viewModel()
    @Inject lateinit var navigator: AuthNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigator.attach(navigationContext)

        val view = LoginView(
            this,
            onRedirect = { viewModel.onRedirect(it) },
            onOpenUrl = { navigator.openUrl(it) })

        viewModel.state
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { view.render(it) }
            .disposeOnDestroy()
    }

    override fun onCancelDialog(dialog: DialogFragment) {
        if (dialog.tag == LoginView.DIALOG_AUTH_ERROR) {
            viewModel.dispatch(ErrorDialogClosedEvent)
        }
    }

    override fun injectDependencies() {
        getComponent<AuthActivityComponent>().inject(this)
    }

    companion object {
        fun route(context: Context): Route = activityRoute<LoginActivity>(context)
    }
}
