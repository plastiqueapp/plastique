package io.plastique.core.converters

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import javax.inject.Inject

class NullFallbackConverter @Inject constructor(private val moshi: Moshi) {
    inline fun <reified T : Any> fromJson(json: String): T? {
        return fromJson(T::class.java, json)
    }

    fun <T : Any> fromJson(type: Class<T>, json: String): T? = try {
        moshi.adapter(type).fromJson(json)
    } catch (e: JsonDataException) {
        null
    }

    fun <T : Any> toJson(value: T): String {
        return moshi.adapter(value.javaClass).toJson(value)
    }
}
