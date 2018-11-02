package io.plastique.core.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

class NullFallbackJsonAdapterFactory(private val delegate: JsonAdapter.Factory) : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        val adapter = delegate.create(type, annotations, moshi)
        return adapter?.let { NullFallbackJsonAdapter(it) }
    }

    private class NullFallbackJsonAdapter<T>(private val delegate: JsonAdapter<T>) : JsonAdapter<T>() {
        override fun fromJson(reader: JsonReader): T? = try {
            delegate.fromJson(reader)
        } catch (e: JsonDataException) {
            reader.skipValue()
            null
        }

        override fun toJson(writer: JsonWriter, value: T?) =
                delegate.toJson(writer, value)
    }
}
