package io.plastique.core.json

import com.squareup.moshi.FromJson
import org.threeten.bp.Instant

class InstantAdapter {
    @FromJson
    fun fromJson(seconds: Long): Instant {
        return Instant.ofEpochSecond(seconds)
    }
}
