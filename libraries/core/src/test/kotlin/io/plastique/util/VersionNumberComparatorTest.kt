package io.plastique.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class VersionNumberComparatorTest {
    @ParameterizedTest
    @CsvSource(
        "1.0, 1.0, 0",
        "1.0, 1.0.0, 0",
        "1.0, 2.0, -1",
        "1.1, 1.2, -1")
    fun test(first: String, second: String, expectedResult: Int) {
        val comparator = VersionNumberComparator()
        assertEquals(expectedResult, comparator.compare(first, second))
        assertEquals(-expectedResult, comparator.compare(second, first))
    }
}
