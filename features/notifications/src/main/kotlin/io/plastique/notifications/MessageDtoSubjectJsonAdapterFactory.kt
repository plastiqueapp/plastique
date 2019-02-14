package io.plastique.notifications

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import io.plastique.api.messages.MessageDto
import java.lang.reflect.Type

class MessageDtoSubjectJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? = when (type) {
        MessageDto.Subject::class.java -> {
            val delegate = moshi.nextAdapter<MessageDto.Subject>(this, type, annotations)
            MessageDtoSubjectJsonAdapter(delegate)
        }
        else -> null
    }

    private class MessageDtoSubjectJsonAdapter(private val delegate: JsonAdapter<MessageDto.Subject>) : JsonAdapter<MessageDto.Subject>() {
        override fun fromJson(reader: JsonReader): MessageDto.Subject? {
            // Workaround for API bug where subject is an empty array
            if (reader.peek() == JsonReader.Token.BEGIN_ARRAY) {
                reader.skipValue()
                return null
            }
            return delegate.fromJson(reader)
        }

        override fun toJson(writer: JsonWriter, value: MessageDto.Subject?) {
            delegate.toJson(writer, value)
        }
    }
}
