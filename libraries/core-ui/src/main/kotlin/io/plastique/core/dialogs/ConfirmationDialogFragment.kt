package io.plastique.core.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.annotation.StringRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.plastique.core.extensions.args
import io.plastique.core.extensions.findCallback

class ConfirmationDialogFragment : BaseDialogFragment() {
    private var onConfirmListener: OnConfirmListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onConfirmListener = findCallback()
    }

    override fun onDetach() {
        super.onDetach()
        onConfirmListener = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val titleId = args.getInt(ARG_TITLE_ID)
        val messageId = args.getInt(ARG_MESSAGE_ID)
        val positiveButtonTextId = args.getInt(ARG_POSITIVE_BUTTON_TEXT_ID)
        val negativeButtonTextId = args.getInt(ARG_NEGATIVE_BUTTON_TEXT_ID)
        val title = if (titleId != 0) getText(titleId) else null
        val message = if (messageId != 0) getText(messageId) else args.getCharSequence(ARG_MESSAGE)

        val listener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> onConfirmListener?.onConfirm(this)
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonTextId, listener)
            .setNegativeButton(negativeButtonTextId, listener)
            .create()
    }

    companion object {
        private const val ARG_TITLE_ID = "title_id"
        private const val ARG_MESSAGE = "message"
        private const val ARG_MESSAGE_ID = "message_id"
        private const val ARG_POSITIVE_BUTTON_TEXT_ID = "positive_button_text_id"
        private const val ARG_NEGATIVE_BUTTON_TEXT_ID = "negative_button_text_id"

        fun newArgs(
            @StringRes titleId: Int,
            @StringRes messageId: Int = 0,
            message: CharSequence? = null,
            @StringRes positiveButtonTextId: Int,
            @StringRes negativeButtonTextInt: Int
        ): Bundle {
            return Bundle().apply {
                putInt(ARG_TITLE_ID, titleId)
                putCharSequence(ARG_MESSAGE, message)
                putInt(ARG_MESSAGE_ID, messageId)
                putInt(ARG_POSITIVE_BUTTON_TEXT_ID, positiveButtonTextId)
                putInt(ARG_NEGATIVE_BUTTON_TEXT_ID, negativeButtonTextInt)
            }
        }
    }
}
