package io.plastique.util

import kotlin.math.max

/**
 * Compares two version numbers. Only numeric parts are supported.
 */
class VersionNumberComparator : Comparator<String> {
    override fun compare(first: String, second: String): Int {
        val firstVersionParts = first.split(".")
        val secondVersionParts = second.split(".")

        val partCount = max(firstVersionParts.size, secondVersionParts.size)
        for (i in 0 until partCount) {
            val firstVersionPart = if (firstVersionParts.size > i) firstVersionParts[i].toInt() else 0
            val secondVersionPart = if (secondVersionParts.size > i) secondVersionParts[i].toInt() else 0
            val result = firstVersionPart.compareTo(secondVersionPart)
            if (result != 0) {
                return result
            }
        }
        return 0
    }
}
