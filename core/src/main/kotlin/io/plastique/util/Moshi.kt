@file:Suppress("MatchingDeclarationName")

package io.plastique.util

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

inline fun <reified T> Moshi.adapter(): JsonAdapter<T> {
    val type = object : TypeToken<T>() {}.type
    return adapter<T>(type)
}

abstract class TypeToken<in T> {
    val type: Type = getSuperclassTypeParameter(javaClass)

    private fun getSuperclassTypeParameter(subclass: Class<*>): Type {
        val superclass = subclass.genericSuperclass as? ParameterizedType
            ?: throw IllegalArgumentException("Super class of $subclass must be parametrized")
        return superclass.actualTypeArguments[0]
    }
}
