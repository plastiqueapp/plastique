package io.plastique.core.text

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spanned
import android.text.style.ImageSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.getSpans

class RichTextView : AppCompatTextView {
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun verifyDrawable(who: Drawable): Boolean {
        if (super.verifyDrawable(who)) return true

        return when (val text = text) {
            is Spanned -> {
                val spans = text.getSpans<ImageSpan>()
                spans.any { it.drawable === who }
            }
            else -> false
        }
    }
}
