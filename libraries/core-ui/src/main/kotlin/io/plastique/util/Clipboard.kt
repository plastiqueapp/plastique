package io.plastique.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.github.technoir42.android.extensions.requireSystemService
import javax.inject.Inject

class Clipboard @Inject constructor(context: Context) {
    private val clipboardManager = context.requireSystemService<ClipboardManager>()

    fun setText(text: String) {
        val clipData = ClipData.newPlainText("", text)
        clipboardManager.setPrimaryClip(clipData)
    }
}
