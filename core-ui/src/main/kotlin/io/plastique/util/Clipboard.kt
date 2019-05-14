package io.plastique.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import io.plastique.core.extensions.requireSystemService
import javax.inject.Inject

class Clipboard @Inject constructor(context: Context) {
    private val clipboardManager = context.requireSystemService<ClipboardManager>()

    fun setText(text: String) {
        val clipData = ClipData.newPlainText("", text)
        clipboardManager.primaryClip = clipData
    }
}
