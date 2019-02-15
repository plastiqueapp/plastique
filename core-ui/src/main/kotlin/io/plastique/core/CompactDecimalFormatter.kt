package io.plastique.core

import java.util.TreeMap

object CompactDecimalFormatter {
    private val suffixes = TreeMap<Int, String>()

    init {
        suffixes[1_000] = "K"
        suffixes[1_000_000] = "M"
        suffixes[1_000_000_000] = "B"
    }

    fun format(value: Int): String {
        if (value == Int.MIN_VALUE) return format(Int.MIN_VALUE + 1)
        if (value < 0) return "-" + format(-value)
        if (value < 1000) return value.toString()

        val (divideBy, suffix) = suffixes.floorEntry(value)
        val truncated = value / (divideBy / 10)
        val hasDecimal = truncated < 100 && truncated / 10.0 != (truncated / 10).toDouble()
        return if (hasDecimal) (truncated / 10.0).toString() + suffix else (truncated / 10).toString() + suffix
    }
}
