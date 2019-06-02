package io.plastique.core.lists

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DiffUtilExtTest {
    @Test
    fun calculateDiff_nullToEmpty() {
        val result = calculateDiff(null, emptyList())
        assertEquals(ListUpdateData.Empty, result)
    }

    @Test
    fun calculateDiff_emptyToEmpty() {
        val result = calculateDiff(emptyList(), emptyList())
        assertEquals(ListUpdateData.Empty, result)
    }

    @Test
    fun calculateDiff_emptyToNonEmpty() {
        val items = listOf(FakeItem("0"), FakeItem("1"))
        val result = calculateDiff(emptyList(), items)
        assertEquals(ListUpdateData.Full(items), result)
    }

    @Test
    fun calculateDiff_nonEmptyToNonEmpty() {
        val newItems = listOf(FakeItem("0"), FakeItem("1"))
        val result = calculateDiff(listOf(FakeItem("0")), newItems)
        assertTrue(result is ListUpdateData.Diff)
        assertEquals(newItems, result.items)
    }

    @Test
    fun calculateDiff_nonEmptyToEqualNonEmpty() {
        val result = calculateDiff(listOf(FakeItem("0")), listOf(FakeItem("0")))
        assertEquals(ListUpdateData.Empty, result)
    }

    private data class FakeItem(override val id: String) : ListItem
}
