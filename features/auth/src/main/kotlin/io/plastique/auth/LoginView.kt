package io.plastique.auth

import android.annotation.SuppressLint
import android.net.Uri
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.github.technoir42.android.extensions.instantiate
import com.github.technoir42.android.extensions.setActionBar
import com.github.technoir42.android.extensions.showAllowingStateLoss
import io.plastique.auth.databinding.ActivityLoginBinding
import io.plastique.core.dialogs.MessageDialogFragment
import io.plastique.core.dialogs.ProgressDialogController
import io.plastique.util.Animations

internal class LoginView(
    private val activity: AppCompatActivity,
    private val onRedirect: (Uri) -> Boolean,
    private val onOpenUrl: (String) -> Unit
) {

    private val binding = ActivityLoginBinding.inflate(activity.layoutInflater)
    private val progressDialogController: ProgressDialogController
    private val onBackPressedCallback: OnBackPressedCallback

    init {
        activity.setContentView(binding.root)
        activity.setActionBar(binding.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }

        binding.webview.apply {
            @SuppressLint("SetJavaScriptEnabled")
            settings.javaScriptEnabled = true
            webChromeClient = LoginWebChromeClient()
            webViewClient = LoginWebViewClient()
        }

        progressDialogController = ProgressDialogController(activity, activity.supportFragmentManager, titleId = R.string.login_progress_title)

        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                binding.webview.goBack()
            }
        }
        activity.onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    fun render(state: LoginViewState) = when (state) {
        LoginViewState.Initial -> Unit

        is LoginViewState.LoadUrl ->
            binding.webview.loadUrl(state.authUrl)

        LoginViewState.InProgress ->
            progressDialogController.isShown = true

        LoginViewState.Success -> {
            progressDialogController.isShown = false
            activity.finish()
        }

        LoginViewState.Error -> {
            progressDialogController.isShown = false
            showAuthErrorDialog()
        }
    }

    private fun showAuthErrorDialog() {
        val dialog = activity.supportFragmentManager.fragmentFactory.instantiate<MessageDialogFragment>(activity, args = MessageDialogFragment.newArgs(
            titleId = R.string.common_error,
            messageId = R.string.login_error_message))
        dialog.showAllowingStateLoss(activity.supportFragmentManager, DIALOG_AUTH_ERROR)
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
            if (onRedirect(uri)) {
                return true
            } else if (uri.host?.endsWith("deviantart.com") == false) {
                onOpenUrl(url)
                return true
            }
            return false
        }

        override fun onPageFinished(view: WebView, url: String) {
            onBackPressedCallback.isEnabled = view.canGoBack()
        }
    }

    companion object {
        const val DIALOG_AUTH_ERROR = "dialog.auth_error"
        private const val MAX_PROGRESS = 100
    }
}
