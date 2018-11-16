package io.plastique.core.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

class FallbackJsonAdapterFactory<T>(private val type: Type, private val fallbackValue: T?) : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (Types.equals(this.type, type)) {
            val adapter: JsonAdapter<T> = moshi.nextAdapter(this, type, annotations)
            return FallbackJsonAdapter(adapter, fallbackValue)
        }
        return null
    }

    private class FallbackJsonAdapter<T>(private val delegate: JsonAdapter<T>, private val fallbackValue: T?) : JsonAdapter<T>() {
        override fun fromJson(reader: JsonReader): T? = try {
            delegate.fromJson(reader)
        } catch (e: JsonDataException) {
            reader.skipValue()
            fallbackValue
        }

        override fun toJson(writer: JsonWriter, value: T?) {
            delegate.toJson(writer, value)
        }
    }
}
