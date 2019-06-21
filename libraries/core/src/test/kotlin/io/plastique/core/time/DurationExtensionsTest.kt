package io.plastique.core.time

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.threeten.bp.Duration

class DurationExtensionsTest {
    @ParameterizedTest
    @MethodSource("printArguments")
    fun print(duration: Duration, expected: String) {
        val result = duration.print()
        assertEquals(expected, result)
    }

    companion object {
        @JvmStatic
        fun printArguments(): List<Arguments> {
            return listOf(
                arguments(Duration.ofHours(38).plusMinutes(21).plusSeconds(45), "38:21:45"),
                arguments(Duration.ofHours(2).plusMinutes(5).plusSeconds(9), "2:05:09"),
                arguments(Duration.ofMinutes(5).plusSeconds(2), "5:02")
            )
        }
    }
}
