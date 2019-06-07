package io.plastique.util

import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

@RunWith(ParameterizedRobolectricTestRunner::class)
class ElapsedTimeFormatterTest(
    private val from: ZonedDateTime,
    private val to: ZonedDateTime,
    private val expected: String
) {
    private val elapsedTimeFormatter = ElapsedTimeFormatter(ApplicationProvider.getApplicationContext())

    @Test
    fun format() {
        val result = elapsedTimeFormatter.format(from, to)
        assertEquals(expected, result)
    }

    companion object {
        @Parameters
        @JvmStatic
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                dateTimeOf(year = 2017, month = 1, dayOfMonth = 1),
                dateTimeOf(year = 2017, month = 1, dayOfMonth = 1, hour = 0, minute = 0, second = 5),
                "just now"),

            arrayOf(
                dateTimeOf(year = 2017, month = 1, dayOfMonth = 1, hour = 1, minute = 0, second = 0),
                dateTimeOf(year = 2017, month = 1, dayOfMonth = 1),
                "just now"),

            arrayOf(
                dateTimeOf(year = 2017, month = 1, dayOfMonth = 1),
                dateTimeOf(year = 2017, month = 1, dayOfMonth = 1, hour = 0, minute = 0, second = 6),
                "6s"),

            arrayOf(
                dateTimeOf(year = 2017, month = 1, dayOfMonth = 1),
                dateTimeOf(year = 2017, month = 1, dayOfMonth = 1, hour = 0, minute = 0, second = 59),
                "59s"),

            arrayOf(
                dateTimeOf(year = 2017, month = 1, dayOfMonth = 1),
                dateTimeOf(year = 2017, month = 1, dayOfMonth = 1, hour = 0, minute = 1, second = 0),
                "1m"),

            arrayOf(
                dateTimeOf(year = 2017, month = 1, dayOfMonth = 1),
                dateTimeOf(year = 2017, month = 1, dayOfMonth = 1, hour = 0, minute = 59, second = 59),
                "59m"),

            arrayOf(
                dateTimeOf(year = 2017, month = 1, dayOfMonth = 1),
                dateTimeOf(year = 2017, month = 1, dayOfMonth = 1, hour = 1, minute = 0, second = 0),
                "1h"),

            arrayOf(
                dateTimeOf(year = 2017, month = 1, dayOfMonth = 1),
                dateTimeOf(year = 2017, month = 1, dayOfMonth = 1, hour = 1, minute = 59, second = 59),
                "1h"),

            arrayOf(
                dateTimeOf(year = 2017, month = 1, dayOfMonth = 1),
                dateTimeOf(year = 2017, month = 1, dayOfMonth = 1, hour = 23, minute = 59, second = 59),
                "23h"),

            arrayOf(
                dateTimeOf(year = 2017, month = 1, dayOfMonth = 1),
                dateTimeOf(year = 2017, month = 1, dayOfMonth = 2),
                "1d"),

            arrayOf(
                dateTimeOf(year = 2017, month = 1, dayOfMonth = 1),
                dateTimeOf(year = 2017, month = 1, dayOfMonth = 8, hour = 23, minute = 59, second = 59),
                "7d"),

            arrayOf(
                dateTimeOf(year = 2017, month = 1, dayOfMonth = 1),
                dateTimeOf(year = 2017, month = 1, dayOfMonth = 9),
                "01 Jan"),

            arrayOf(
                dateTimeOf(year = 2016, month = 12, dayOfMonth = 31),
                dateTimeOf(year = 2017, month = 1, dayOfMonth = 9),
                "31 Dec 16"))

        @Suppress("LongParameterList")
        private fun dateTimeOf(year: Int, month: Int, dayOfMonth: Int, hour: Int = 0, minute: Int = 0, second: Int = 0): ZonedDateTime {
            return ZonedDateTime.of(year, month, dayOfMonth, hour, minute, second, 0, ZoneId.systemDefault())
        }
    }
}
