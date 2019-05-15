package io.plastique.core.adapters;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;

import io.plastique.api.common.StringEnum;

public final class StringEnumJsonAdapter<T extends Enum<T> & StringEnum> extends JsonAdapter<T> {
    private final Class<T> enumType;
    private final String[] nameStrings;
    private final T[] constants;
    private final JsonReader.Options options;

    private StringEnumJsonAdapter(Class<T> enumType) {
        this.enumType = enumType;

        constants = enumType.getEnumConstants();
        nameStrings = new String[constants.length];
        for (int i = 0; i < constants.length; i++) {
            nameStrings[i] = constants[i].getValue();
        }

        options = JsonReader.Options.of(nameStrings);
    }

    @Override
    public T fromJson(JsonReader reader) throws IOException {
        final int index = reader.selectString(options);
        if (index != -1) return constants[index];

        // We can consume the string safely, we are terminating anyway.
        final String path = reader.getPath();
        final String name = reader.nextString();
        throw new JsonDataException("Expected one of "
                + Arrays.asList(nameStrings) + " but was " + name + " at path " + path);
    }

    @Override
    public void toJson(JsonWriter writer, T value) throws IOException {
        writer.value(value.getValue());
    }

    @Override
    public String toString() {
        return "JsonAdapter(" + enumType.getName() + ")";
    }

    public static final class Factory implements JsonAdapter.Factory {
        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public JsonAdapter<?> create(Type type, Set<? extends Annotation> annotations, Moshi moshi) {
            final Class<?> rawType = Types.getRawType(type);
            if (rawType.isEnum() && StringEnum.class.isAssignableFrom(rawType)) {
                return new StringEnumJsonAdapter(rawType).nullSafe();
            }
            return null;
        }
    }
}
