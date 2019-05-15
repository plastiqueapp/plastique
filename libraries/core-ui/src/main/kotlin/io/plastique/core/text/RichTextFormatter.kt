package io.plastique.core.text

import android.text.Spanned
import androidx.core.text.HtmlCompat
import javax.inject.Inject

class RichTextFormatter @Inject constructor() {
    fun format(source: String): Spanned {
        return HtmlCompat.fromHtml(source, 0)
    }
}
