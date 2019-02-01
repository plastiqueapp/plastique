package io.plastique.core.dialogs

import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import io.plastique.core.extensions.showAllowingStateLoss
import io.plastique.core.ui.R

class ProgressDialogController(
    private val fragmentManager: FragmentManager,
    @StringRes private val titleId: Int = 0,
    @StringRes private val messageId: Int = R.string.common_message_please_wait,
    private val tag: String = DEFAULT_TAG
) {

    private var fragment: DialogFragment? = null

    init {
        fragment = fragmentManager.findFragmentByTag(tag) as DialogFragment?
    }

    var isShown: Boolean
        get() = fragment != null
        set(value) {
            val fragment = this.fragment
            if (value && fragment == null) {
                val dialog = ProgressDialogFragment.newInstance(titleId, messageId)
                dialog.showAllowingStateLoss(fragmentManager, tag)
                this.fragment = dialog
            } else if (!value && fragment != null) {
                fragment.dismissAllowingStateLoss()
                this.fragment = null
            }
        }

    companion object {
        private const val DEFAULT_TAG = "dialog.progress"
    }
}
