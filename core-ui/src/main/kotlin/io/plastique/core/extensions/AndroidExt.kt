package io.plastique.core.extensions

import android.os.Parcelable

inline fun <reified T : Parcelable> getParcelableCreator(): Parcelable.Creator<T> {
    val field = T::class.java.getDeclaredField("CREATOR")
    @Suppress("UNCHECKED_CAST")
    return field.get(null) as Parcelable.Creator<T>
}
