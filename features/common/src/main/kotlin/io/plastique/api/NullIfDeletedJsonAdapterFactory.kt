package io.plastique.api

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.plastique.api.common.NullIfDeleted
import java.lang.reflect.Type

class NullIfDeletedJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        val delegateAnnotations = Types.nextAnnotations(annotations, NullIfDeleted::class.java) ?: return null
        val delegate = moshi.nextAdapter<Any>(this, type, delegateAnnotations)
        return NullIfDeletedJsonAdapter(delegate)
    }

    private class NullIfDeletedJsonAdapter<T>(private val delegate: JsonAdapter<T>) : JsonAdapter<T>() {
        private val options = JsonReader.Options.of("is_deleted")

        override fun toJson(writer: JsonWriter, value: T?) {
            delegate.toJson(writer, value)
        }

        override fun fromJson(reader: JsonReader): T? {
            if (isDeleted(reader.peekJson())) {
                reader.skipValue()
                return null
            }
            return delegate.fromJson(reader)
        }

        private fun isDeleted(reader: JsonReader): Boolean {
            reader.beginObject()
            var isDeleted = false
            while (reader.hasNext()) {
                if (reader.selectName(options) != -1) {
                    isDeleted = reader.nextBoolean()
                    break
                }
                reader.skipName()
                reader.skipValue()
            }
            reader.close()
            return isDeleted
        }
    }
}
