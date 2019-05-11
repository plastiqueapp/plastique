package io.plastique.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

class CompactDecimalFormatterTest {
    @ParameterizedTest(name = "format({0}) = \"{1}\"")
    @MethodSource("arguments")
    fun format(value: Int, expectedResult: String) {
        val result = CompactDecimalFormatter.format(value)
        assertEquals(expectedResult, result)
    }

    companion object {
        @JvmStatic
        fun arguments(): Array<Arguments> {
            return arrayOf(
                arguments(999, "999"),
                arguments(-999, "-999"),
                arguments(1000, "1K"),
                arguments(1600, "1.6K"),
                arguments(-1600, "-1.6K"),
                arguments(1000000, "1M"),
                arguments(1600000, "1.6M"),
                arguments(1600000000, "1.6B"),
                arguments(Int.MAX_VALUE, "2.1B"),
                arguments(Int.MIN_VALUE, "-2.1B"))
        }
    }
}
