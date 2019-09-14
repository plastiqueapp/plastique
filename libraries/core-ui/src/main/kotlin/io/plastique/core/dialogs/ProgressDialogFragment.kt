package io.plastique.core.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.StringRes

class ProgressDialogFragment : BaseDialogFragment() {
    init {
        isCancelable = false
    }

    @Suppress("DEPRECATION")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()
        val titleId = args.getInt(ARG_TITLE_ID)
        val messageId = args.getInt(ARG_MESSAGE_ID)

        val dialog = android.app.ProgressDialog(requireContext())
        if (titleId != 0) {
            dialog.setTitle(getString(titleId))
        }
        dialog.setMessage(getString(messageId))
        return dialog
    }

    companion object {
        private const val ARG_TITLE_ID = "title_id"
        private const val ARG_MESSAGE_ID = "message_id"

        fun newArgs(@StringRes titleId: Int, @StringRes messageId: Int): Bundle {
            return Bundle().apply {
                putInt(ARG_TITLE_ID, titleId)
                putInt(ARG_MESSAGE_ID, messageId)
            }
        }
    }
}
