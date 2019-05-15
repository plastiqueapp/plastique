package io.plastique.core.extensions

import android.content.Context
import android.os.Parcelable
import androidx.core.content.ContextCompat

inline fun <reified T : Any> Context.requireSystemService(): T {
    return checkNotNull(ContextCompat.getSystemService(this, T::class.java)) {
        "Required system service ${T::class.java.simpleName} is null"
    }
}

inline fun <reified T : Parcelable> getParcelableCreator(): Parcelable.Creator<T> {
    val field = T::class.java.getDeclaredField("CREATOR")
    @Suppress("UNCHECKED_CAST")
    return field.get(null) as Parcelable.Creator<T>
}
