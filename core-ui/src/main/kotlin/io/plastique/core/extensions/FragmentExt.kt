package io.plastique.core.extensions

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentManager

inline val Fragment.args: Bundle
    get() = arguments!!

val Fragment.actionBar: ActionBar
    get() = (activity as AppCompatActivity).supportActionBar!!

val Fragment.isRemovingSelfOrParent: Boolean
    get() = isRemoving || parentFragment?.isRemovingSelfOrParent ?: false

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

inline fun <reified T : Fragment> FragmentFactory.instantiate(context: Context, args: Bundle? = null): T {
    return instantiate(context.classLoader, T::class.java.name).apply { arguments = args } as T
}
