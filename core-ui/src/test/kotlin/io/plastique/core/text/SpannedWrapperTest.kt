package io.plastique.core.text

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.text.style.UnderlineSpan
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SpannedWrapperTest {
    private val spannableString = SpannableString.valueOf("foo").apply {
        setSpan(UnderlineSpan(), 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        setSpan(ImageSpan(ApplicationProvider.getApplicationContext<Context>(), android.R.drawable.ic_delete), 1, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    @Test
    fun equals_comparesOnlyContent() {
        val first = SpannedWrapper(spannableString)
        val second = SpannedWrapper(SpannableString.valueOf(spannableString.toString()))
        assertTrue(first == second)
    }

    @Test
    fun hashCode_equalsContentHashCode() {
        assertEquals(spannableString.toString().hashCode(), SpannedWrapper(spannableString).hashCode())
    }

    @Test
    fun toString_returnsContent() {
        assertEquals(spannableString.toString(), SpannedWrapper(spannableString).toString())
    }
}
