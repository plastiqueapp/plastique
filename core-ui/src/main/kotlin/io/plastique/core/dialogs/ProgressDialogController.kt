package io.plastique.core.dialogs

import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import io.plastique.core.extensions.showAllowingStateLoss
import io.plastique.core.ui.R

class ProgressDialogController(
    private val fragmentManager: FragmentManager,
    private val tag: String = DEFAULT_TAG
) {

    fun show(@StringRes titleId: Int, @StringRes messageId: Int = R.string.common_message_please_wait) {
        if (fragment == null) {
            val fragment = ProgressDialogFragment.newInstance(titleId, messageId)
            fragment.showAllowingStateLoss(fragmentManager, tag)
        }
    }

    fun dismiss() {
        fragment?.dismissAllowingStateLoss()
    }

    private val fragment: DialogFragment?
        get() = fragmentManager.findFragmentByTag(tag) as DialogFragment?

    companion object {
        private const val DEFAULT_TAG = "dialog.progress"
    }
}
