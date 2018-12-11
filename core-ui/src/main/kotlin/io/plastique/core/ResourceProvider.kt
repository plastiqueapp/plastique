package io.plastique.core

import android.content.Context
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import javax.inject.Inject

class ResourceProvider @Inject constructor(private val context: Context) {
    fun getString(@StringRes resourceId: Int): String {
        return context.getString(resourceId)
    }

    fun getString(@StringRes resourceId: Int, vararg args: Any?): String {
        return context.getString(resourceId, *args)
    }

    fun getStringArray(@ArrayRes resourceId: Int): Array<String> {
        return context.resources.getStringArray(resourceId)
    }
}
