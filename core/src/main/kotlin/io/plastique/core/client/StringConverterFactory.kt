package io.plastique.core.client

import io.plastique.api.common.StringEnum
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class StringConverterFactory : Converter.Factory() {
    override fun stringConverter(type: Type, annotations: Array<Annotation>, retrofit: Retrofit): Converter<*, String>? {
        val rawType = Converter.Factory.getRawType(type)
        return when {
            StringEnum::class.java.isAssignableFrom(rawType) -> StringEnumConverter
            else -> null
        }
    }
}

object StringEnumConverter : Converter<StringEnum, String> {
    override fun convert(value: StringEnum): String = value.value
}
