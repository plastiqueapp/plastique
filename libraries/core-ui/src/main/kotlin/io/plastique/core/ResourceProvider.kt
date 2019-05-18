package io.plastique.core

import android.content.Context
import androidx.annotation.ArrayRes
import javax.inject.Inject

class ResourceProvider @Inject constructor(private val context: Context) {
    fun getStringArray(@ArrayRes resourceId: Int): Array<String> {
        return context.resources.getStringArray(resourceId)
    }
}
