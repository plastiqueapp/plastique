package io.plastique.util

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

inline fun <reified T> Moshi.adapter(): JsonAdapter<T> {
    val type = object : TypeToken<T>() {}.type
    return adapter<T>(type)
}

@Suppress("unused")
abstract class TypeToken<in T> {
    val type: Type = getSuperclassTypeParameter(javaClass)

    private fun getSuperclassTypeParameter(subclass: Class<*>): Type {
        val superclass = subclass.genericSuperclass
        if (superclass is Class<*>) {
            throw RuntimeException("Missing type parameter")
        }
        val parameterized = superclass as ParameterizedType
        return parameterized.actualTypeArguments[0]
    }
}
