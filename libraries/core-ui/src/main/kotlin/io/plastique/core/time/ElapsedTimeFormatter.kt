package io.plastique.core.time

import android.content.Context
import io.plastique.core.ui.R
import org.threeten.bp.Clock
import org.threeten.bp.Duration
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class ElapsedTimeFormatter @Inject constructor(
    private val context: Context,
    private val clock: Clock
) {
    fun format(from: ZonedDateTime): String {
        return format(from, ZonedDateTime.now(clock))
    }

    @Suppress("MagicNumber")
    fun format(from: ZonedDateTime, to: ZonedDateTime): String {
        val elapsed = Duration.between(from, to)
        return when {
            elapsed.seconds <= 5 -> context.getString(R.string.common_just_now)
            elapsed.toMinutes() < 1 -> context.getString(R.string.common_abbr_seconds, elapsed.seconds)
            elapsed.toHours() < 1 -> context.getString(R.string.common_abbr_minutes, elapsed.toMinutes())
            elapsed.toDays() < 1 -> context.getString(R.string.common_abbr_hours, elapsed.toHours())
            elapsed.toDays() <= 7 -> context.getString(R.string.common_abbr_days, elapsed.toDays())
            from.year == to.year -> DAY_MONTH.format(from)
            else -> DAY_MONTH_YEAR.format(from)
        }
    }

    companion object {
        private val DAY_MONTH = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH)
        private val DAY_MONTH_YEAR = DateTimeFormatter.ofPattern("dd MMM yy", Locale.ENGLISH)
    }
}
