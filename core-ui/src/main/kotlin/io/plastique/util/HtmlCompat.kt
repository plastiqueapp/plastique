package io.plastique.util

import android.text.Html
import android.text.Spanned

object HtmlCompat {
    fun fromHtml(source: String): Spanned {
        @Suppress("DEPRECATION")
        return Html.fromHtml(source)
    }
}
