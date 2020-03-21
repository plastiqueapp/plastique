package io.plastique.auth

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.DialogFragment
import com.github.technoir42.android.extensions.instantiate
import com.github.technoir42.android.extensions.setActionBar
import com.github.technoir42.android.extensions.showAllowingStateLoss
import io.plastique.auth.LoginEvent.ErrorDialogDismissedEvent
import io.plastique.auth.databinding.ActivityLoginBinding
import io.plastique.core.BaseActivity
import io.plastique.core.dialogs.MessageDialogFragment
import io.plastique.core.dialogs.OnDismissDialogListener
import io.plastique.core.dialogs.ProgressDialogController
import io.plastique.core.mvvm.viewModel
import io.plastique.core.navigation.Route
import io.plastique.core.navigation.activityRoute
import io.plastique.inject.getComponent
import io.plastique.util.Animations
import io.plastique.util.Intents
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

class LoginActivity : BaseActivity(), OnDismissDialogListener {
    private val viewModel: LoginViewModel by viewModel()

    private lateinit var binding: ActivityLoginBinding
    private lateinit var progressDialogController: ProgressDialogController

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setActionBar(binding.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }

        binding.webview.apply {
            settings.javaScriptEnabled = true
            webChromeClient = LoginWebChromeClient()
            webViewClient = LoginWebViewClient()
        }

        progressDialogController = ProgressDialogController(this, supportFragmentManager, titleId = R.string.login_progress_title)

        viewModel.state
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { renderState(it) }
            .disposeOnDestroy()
    }

    override fun onBackPressed() {
        if (binding.webview.canGoBack()) {
            binding.webview.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDismissDialog(dialog: DialogFragment) {
        when (dialog.tag) {
            DIALOG_AUTH_ERROR -> viewModel.dispatch(ErrorDialogDismissedEvent)
        }
    }

    private fun renderState(state: LoginViewState) = when (state) {
        LoginViewState.Initial -> Unit

        is LoginViewState.LoadUrl ->
            binding.webview.loadUrl(state.authUrl)

        LoginViewState.InProgress ->
            progressDialogController.isShown = true

        LoginViewState.Success -> {
            progressDialogController.isShown = false
            finish()
        }

        LoginViewState.Error -> {
            progressDialogController.isShown = false
            val dialog = supportFragmentManager.fragmentFactory.instantiate<MessageDialogFragment>(this, args = MessageDialogFragment.newArgs(
                titleId = R.string.common_error,
                messageId = R.string.login_error_message))
            dialog.showAllowingStateLoss(supportFragmentManager, DIALOG_AUTH_ERROR)
        }
    }

    override fun injectDependencies() {
        getComponent<AuthActivityComponent>().inject(this)
    }

    private fun setLoadProgress(progress: Int) {
        binding.progress.progress = progress
        if (progress != MAX_PROGRESS) {
            binding.progress.alpha = 1.0f
            binding.progress.visibility = View.VISIBLE
        } else {
            Animations.fadeOut(binding.progress, Animations.DURATION_SHORT)
        }
    }

    private inner class LoginWebChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            setLoadProgress(newProgress)
        }
    }

    private inner class LoginWebViewClient : WebViewClient() {
        @Suppress("OverridingDeprecatedMember")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            val uri = Uri.parse(url)
            if (viewModel.onRedirect(uri)) {
                return true
            } else if (uri.host?.endsWith("deviantart.com") == false) {
                val intent = Intents.view(uri)
                try {
                    startActivity(intent)
                    return true
                } catch (e: ActivityNotFoundException) {
                    Timber.e(e)
                }
            }
            return false
        }
    }

    companion object {
        private const val DIALOG_AUTH_ERROR = "dialog.auth_error"
        private const val MAX_PROGRESS = 100

        fun route(context: Context): Route = activityRoute<LoginActivity>(context)
    }
}
