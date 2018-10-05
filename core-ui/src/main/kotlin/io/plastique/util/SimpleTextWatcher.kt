package io.plastique.util

import android.text.Editable
import android.text.TextWatcher

abstract class SimpleTextWatcher : TextWatcher {
    /**
     * {@inheritDoc}
     */
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
    }

    /**
     * {@inheritDoc}
     */
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    }

    /**
     * {@inheritDoc}
     */
    override fun afterTextChanged(s: Editable) {
    }
}
