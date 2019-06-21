package io.plastique.core.time

import org.threeten.bp.Duration

fun Duration.print(): String {
    val hours = seconds / SECONDS_PER_HOUR
    val minutes = (seconds % SECONDS_PER_HOUR / SECONDS_PER_MINUTE).toInt()
    val seconds = (seconds % SECONDS_PER_MINUTE).toInt()

    return buildString {
        if (hours != 0L) {
            append(hours)
            append(':')
            append("%02d".format(minutes))
        } else {
            append(minutes)
        }
        append(':')
        append("%02d".format(seconds))
    }
}

private const val SECONDS_PER_MINUTE = 60
private const val MINUTES_PER_HOUR = 60
private const val SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR
