package io.plastique.core.dialogs

import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentManager
import io.plastique.core.extensions.instantiate
import io.plastique.core.extensions.showAllowingStateLoss
import io.plastique.core.ui.R

class ProgressDialogController(
    private val context: Context,
    private val fragmentManager: FragmentManager,
    @StringRes private val titleId: Int = 0,
    @StringRes private val messageId: Int = R.string.common_message_please_wait,
    private val tag: String = DEFAULT_TAG
) {

    private var fragment: ProgressDialogFragment? = null

    init {
        fragment = fragmentManager.findFragmentByTag(tag) as ProgressDialogFragment?
    }

    var isShown: Boolean
        get() = fragment != null
        set(value) {
            val fragment = this.fragment
            if (value && fragment == null) {
                val dialog = fragmentManager.fragmentFactory.instantiate<ProgressDialogFragment>(context,
                        args = ProgressDialogFragment.newArgs(titleId, messageId))
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
