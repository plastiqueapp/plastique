package io.plastique.core.extensions

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

inline var Fragment.args: Bundle
    get() = arguments!!
    set(value) {
        arguments = value
    }

val Fragment.isRemovingSelfOrParent: Boolean
    get() = isRemoving || parentFragment?.isRemovingSelfOrParent ?: false

inline fun <T : Fragment> T.withArguments(crossinline block: Bundle.() -> Unit): T {
    arguments = Bundle().apply(block)
    return this
}

inline fun <reified T> Fragment.findCallback(): T? {
    return if (parentFragment != null) {
        parentFragment as? T
    } else {
        requireActivity() as? T
    }
}

fun DialogFragment.showAllowingStateLoss(fragmentManager: FragmentManager, tag: String) {
    fragmentManager.beginTransaction()
            .add(this, tag)
            .commitAllowingStateLoss()
}
