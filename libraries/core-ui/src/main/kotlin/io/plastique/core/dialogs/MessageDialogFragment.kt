package io.plastique.core.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.StringRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.plastique.core.extensions.args
import io.plastique.core.ui.R

class MessageDialogFragment : BaseDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val titleId = args.getInt(ARG_TITLE_ID)
        val messageId = args.getInt(ARG_MESSAGE_ID)
        val title = if (titleId != 0) getString(titleId) else null
        val message = if (messageId != 0) getString(messageId) else args.getString(ARG_MESSAGE)

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.common_button_ok, null)
            .create()
    }

    companion object {
        private const val ARG_TITLE_ID = "title_id"
        private const val ARG_MESSAGE = "message"
        private const val ARG_MESSAGE_ID = "message_id"

        fun newArgs(@StringRes titleId: Int, @StringRes messageId: Int): Bundle {
            return Bundle().apply {
                putInt(ARG_TITLE_ID, titleId)
                putInt(ARG_MESSAGE_ID, messageId)
            }
        }
    }
}
