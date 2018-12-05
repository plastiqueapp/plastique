package io.plastique.core.text

import android.text.Spanned
import android.text.SpannedString

class SpannedWrapper(val value: Spanned) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SpannedWrapper
        return value === other.value ||
                contentEquals(value, other.value)
    }

    override fun hashCode(): Int {
        var hash = 0
        if (value.isNotEmpty()) {
            val v = value
            for (i in v.indices) {
                hash = 31 * hash + v[i].toInt()
            }
        }
        return hash
    }

    override fun toString(): String {
        return value.toString()
    }

    private fun contentEquals(cs1: CharSequence, cs2: CharSequence): Boolean {
        if (cs1.length != cs2.length) {
            return false
        }
        for (i in 0 until cs1.length) {
            if (cs1[i] != cs2[i]) {
                return false
            }
        }
        return true
    }

    companion object {
        val EMPTY = SpannedWrapper(SpannedString.valueOf(""))
    }
}
