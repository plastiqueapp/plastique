package io.plastique.core.extensions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class KotlinExtTest {
    @Test
    fun truncate() {
        assertEquals("abc", "abc".truncate(5))
        assertEquals("abcde", "abcde".truncate(5))
        assertEquals("abcde...", "abcdef".truncate(5))

        assertThrows<IllegalArgumentException> { "abc".truncate(0) }
    }
}
