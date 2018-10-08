package io.plastique.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.core.content.getSystemService
import javax.inject.Inject

class Clipboard @Inject constructor(context: Context) {
    private val clipboardManager = context.getSystemService<ClipboardManager>()!!

    fun setText(text: String) {
        val clipData = ClipData.newPlainText("", text)
        clipboardManager.primaryClip = clipData
    }
}
