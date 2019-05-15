package io.plastique.core.dialogs

import android.content.Context
import android.content.DialogInterface
import androidx.fragment.app.DialogFragment
import io.plastique.core.extensions.findCallback

abstract class BaseDialogFragment : DialogFragment() {
    private var onCancelDialogListener: OnCancelDialogListener? = null
    private var onDismissDialogListener: OnDismissDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onCancelDialogListener = findCallback<OnCancelDialogListener>()
        onDismissDialogListener = findCallback<OnDismissDialogListener>()
    }

    override fun onDetach() {
        super.onDetach()
        onCancelDialogListener = null
        onDismissDialogListener = null
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        onCancelDialogListener?.onCancelDialog(this)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissDialogListener?.onDismissDialog(this)
    }
}
