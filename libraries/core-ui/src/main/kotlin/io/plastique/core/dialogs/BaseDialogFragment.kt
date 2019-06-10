package io.plastique.core.dialogs

import android.content.Context
import android.content.DialogInterface
import androidx.fragment.app.DialogFragment
import com.github.technoir42.android.extensions.getCallback

abstract class BaseDialogFragment : DialogFragment() {
    private var onCancelDialogListener: OnCancelDialogListener? = null
    private var onDismissDialogListener: OnDismissDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onCancelDialogListener = getCallback()
        onDismissDialogListener = getCallback()
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
