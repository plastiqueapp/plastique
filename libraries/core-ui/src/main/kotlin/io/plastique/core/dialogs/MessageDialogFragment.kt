package io.plastique.core.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.StringRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.plastique.core.ui.R

class MessageDialogFragment : BaseDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()
        val titleId = args.getInt(ARG_TITLE_ID)
        val messageId = args.getInt(ARG_MESSAGE_ID)
        val buttonTextId = args.getInt(ARG_BUTTON_TEXT_ID)
        val title = if (titleId != 0) getText(titleId) else null
        val message = if (messageId != 0) getText(messageId) else args.getCharSequence(ARG_MESSAGE)

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(buttonTextId, null)
            .create()
    }

    companion object {
        private const val ARG_TITLE_ID = "title_id"
        private const val ARG_MESSAGE = "message"
        private const val ARG_MESSAGE_ID = "message_id"
        private const val ARG_BUTTON_TEXT_ID = "button_text_id"

        fun newArgs(
            @StringRes titleId: Int,
            @StringRes messageId: Int = 0,
            message: CharSequence? = null,
            @StringRes buttonTextId: Int = R.string.common_button_ok
        ): Bundle {
            return Bundle().apply {
                putInt(ARG_TITLE_ID, titleId)
                putInt(ARG_MESSAGE_ID, messageId)
                putCharSequence(ARG_MESSAGE, message)
                putInt(ARG_BUTTON_TEXT_ID, buttonTextId)
            }
        }
    }
}
