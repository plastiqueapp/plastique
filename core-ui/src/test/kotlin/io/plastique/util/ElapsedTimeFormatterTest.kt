package io.plastique.util

import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters
import org.robolectric.RuntimeEnvironment
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

@RunWith(ParameterizedRobolectricTestRunner::class)
class ElapsedTimeFormatterTest(
    private val from: ZonedDateTime,
    private val to: ZonedDateTime,
    private val expected: String
) {
    @Test
    fun format() {
        val result = ElapsedTimeFormatter.format(RuntimeEnvironment.application, from, to)
        assertEquals(expected, result)
    }

    companion object {
        @Parameters
        @JvmStatic
        fun data(): Collection<Array<Any>> = listOf(
                arrayOf(ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()),
                        ZonedDateTime.of(2017, 1, 1, 0, 0, 5, 0, ZoneId.systemDefault()),
                        "just now"),

                arrayOf(ZonedDateTime.of(2017, 1, 1, 1, 0, 0, 0, ZoneId.systemDefault()),
                        ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()),
                        "just now"),

                arrayOf(ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()),
                        ZonedDateTime.of(2017, 1, 1, 0, 0, 6, 0, ZoneId.systemDefault()),
                        "6s"),

                arrayOf(ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()),
                        ZonedDateTime.of(2017, 1, 1, 0, 0, 59, 0, ZoneId.systemDefault()),
                        "59s"),

                arrayOf(ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()),
                        ZonedDateTime.of(2017, 1, 1, 0, 1, 0, 0, ZoneId.systemDefault()),
                        "1m"),

                arrayOf(ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()),
                        ZonedDateTime.of(2017, 1, 1, 0, 59, 59, 0, ZoneId.systemDefault()),
                        "59m"),

                arrayOf(ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()),
                        ZonedDateTime.of(2017, 1, 1, 1, 0, 0, 0, ZoneId.systemDefault()),
                        "1h"),

                arrayOf(ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()),
                        ZonedDateTime.of(2017, 1, 1, 1, 59, 59, 0, ZoneId.systemDefault()),
                        "1h"),

                arrayOf(ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()),
                        ZonedDateTime.of(2017, 1, 1, 23, 59, 59, 0, ZoneId.systemDefault()),
                        "23h"),

                arrayOf(ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()),
                        ZonedDateTime.of(2017, 1, 2, 0, 0, 0, 0, ZoneId.systemDefault()),
                        "1d"),

                arrayOf(ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()),
                        ZonedDateTime.of(2017, 1, 8, 23, 59, 59, 0, ZoneId.systemDefault()),
                        "7d"),

                arrayOf(ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()),
                        ZonedDateTime.of(2017, 1, 9, 0, 0, 0, 0, ZoneId.systemDefault()),
                        "01 Jan"),

                arrayOf(ZonedDateTime.of(2016, 12, 31, 0, 0, 0, 0, ZoneId.systemDefault()),
                        ZonedDateTime.of(2017, 1, 9, 0, 0, 0, 0, ZoneId.systemDefault()),
                        "31 Dec 16"))
    }
}
