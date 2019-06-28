package io.plastique.util

import kotlin.math.ln
import kotlin.math.pow

@Suppress("MagicNumber")
object ByteCountFormatter {
    fun format(bytes: Long, si: Boolean = false): String {
        val unit = if (si) 1000 else 1024
        if (bytes < unit) return "$bytes B"
        val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
        return String.format("%.1f %sB", bytes / unit.toDouble().pow(exp.toDouble()), pre)
    }
}
