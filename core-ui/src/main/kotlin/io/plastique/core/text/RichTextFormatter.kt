package io.plastique.core.text

import androidx.core.text.HtmlCompat
import javax.inject.Inject

class RichTextFormatter @Inject constructor() {
    fun format(source: String): CharSequence {
        return HtmlCompat.fromHtml(source, 0)
    }
}
