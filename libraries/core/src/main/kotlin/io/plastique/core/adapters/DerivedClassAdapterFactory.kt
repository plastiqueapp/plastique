package io.plastique.core.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

class DerivedClassAdapterFactory<T : Any>(
    private val baseClass: Class<T>,
    private val actualClass: Class<out T>
) : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        return when (Types.getRawType(type)) {
            baseClass -> moshi.adapter(actualClass)
            else -> null
        }
    }
}
