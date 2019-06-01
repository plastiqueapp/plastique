package io.plastique.auth

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import io.plastique.auth.LoginEvent.ErrorDialogDismissedEvent
import io.plastique.core.dialogs.MessageDialogFragment
import io.plastique.core.dialogs.OnDismissDialogListener
import io.plastique.core.dialogs.ProgressDialogController
import io.plastique.core.extensions.instantiate
import io.plastique.core.extensions.setActionBar
import io.plastique.core.extensions.showAllowingStateLoss
import io.plastique.core.mvvm.MvvmActivity
import io.plastique.inject.getComponent
import io.plastique.util.Animations
import io.plastique.util.Intents
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

class LoginActivity : MvvmActivity<LoginViewModel>(LoginViewModel::class.java), OnDismissDialogListener {
    private lateinit var progressBar: ProgressBar
    private lateinit var webView: WebView
    private lateinit var progressDialogController: ProgressDialogController

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }

        progressBar = findViewById(R.id.progress)
        webView = findViewById(R.id.webview)
        webView.settings.javaScriptEnabled = true
        webView.webChromeClient = LoginWebChromeClient()
        webView.webViewClient = LoginWebViewClient()

        progressDialogController = ProgressDialogController(this, supportFragmentManager, titleId = R.string.login_progress_title)

        viewModel.state
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { renderState(it) }
            .disposeOnDestroy()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
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
            webView.loadUrl(state.authUrl)

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
        progressBar.progress = progress
        if (progress != MAX_PROGRESS) {
            progressBar.alpha = 1.0f
            progressBar.visibility = View.VISIBLE
        } else {
            Animations.fadeOut(progressBar, Animations.DURATION_SHORT)
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

        fun createIntent(context: Context): Intent {
            return Intent(context, LoginActivity::class.java)
        }
    }
}
