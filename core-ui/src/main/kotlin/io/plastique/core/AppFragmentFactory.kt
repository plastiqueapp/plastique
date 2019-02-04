package io.plastique.core

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import javax.inject.Inject

class AppFragmentFactory @Inject constructor() : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String, args: Bundle?): Fragment {
        val fragmentClass = loadFragmentClass(classLoader, className)
        val fragment = try {
            fragmentClass.getConstructor().newInstance()
        } catch (e: Exception) {
            throw Fragment.InstantiationException("Unable to instantiate fragment $className", e)
        }
        if (args != null) {
            args.classLoader = fragment.javaClass.classLoader
            fragment.arguments = args
        }
        return fragment
    }
}
