package io.plastique.util

import android.content.SharedPreferences
import io.reactivex.Observable
import java.lang.reflect.Type

interface Preferences {
    fun getBoolean(key: String, defaultValue: Boolean): Boolean

    fun getInt(key: String, defaultValue: Int): Int

    fun getLong(key: String, defaultValue: Long): Long

    fun getFloat(key: String, defaultValue: Float): Float

    fun getString(key: String): String?

    fun getString(key: String, defaultValue: String): String

    fun <T : Any> get(key: String, defaultValue: T): T

    fun edit(action: MutablePreferences.() -> Unit)

    fun observable(): ObservablePreferences

    interface Converter<T : Any> {
        fun fromString(string: String, defaultValue: T): T

        fun toString(value: T): String
    }

    class Builder {
        private var sharedPreferences: SharedPreferences? = null
        private val converters = mutableMapOf<Class<*>, Converter<*>>()

        fun sharedPreferences(sharedPreferences: SharedPreferences): Builder {
            this.sharedPreferences = sharedPreferences
            return this
        }

        fun <T : Any> addConverter(valueClass: Class<T>, converter: Converter<T>): Builder {
            converters[valueClass] = converter
            return this
        }

        fun build(): Preferences = PreferencesImpl(
                sharedPreferences = sharedPreferences ?: throw IllegalStateException("SharedPreferences is required"),
                converters = converters)
    }
}

interface MutablePreferences : Preferences {
    fun putBoolean(key: String, value: Boolean)

    fun putInt(key: String, value: Int)

    fun putLong(key: String, value: Long)

    fun putFloat(key: String, value: Float)

    fun putString(key: String, value: String)

    fun <T : Any> put(key: String, value: T)

    fun remove(key: String)

    fun clear()
}

interface ObservablePreferences {
    fun getBoolean(key: String, defaultValue: Boolean): Observable<Boolean>

    fun getInt(key: String, defaultValue: Int): Observable<Int>

    fun getLong(key: String, defaultValue: Long): Observable<Long>

    fun getFloat(key: String, defaultValue: Float): Observable<Float>

    fun getString(key: String, defaultValue: String): Observable<String>

    fun <T : Any> get(key: String, defaultValue: T): Observable<T>
}

private open class PreferencesImpl(
    private val sharedPreferences: SharedPreferences,
    private val converters: Map<Class<*>, Preferences.Converter<*>>
) : Preferences {
    private val observablePreferences: ObservablePreferences by lazy(LazyThreadSafetyMode.NONE) { ObservablePreferencesImpl() }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return sharedPreferences.getFloat(key, defaultValue)
    }

    override fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    override fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue)!!
    }

    override fun <T : Any> get(key: String, defaultValue: T): T {
        val converter = getConverter<T>(defaultValue.javaClass)
        val stringValue: String? = sharedPreferences.getString(key, null)
        return if (stringValue != null) converter.fromString(stringValue, defaultValue) else defaultValue
    }

    override fun edit(action: MutablePreferences.() -> Unit) {
        val editor = sharedPreferences.edit()
        action(MutablePreferencesImpl(editor))
        editor.apply()
    }

    override fun observable(): ObservablePreferences = observablePreferences

    private fun <T : Any> getConverter(type: Type): Preferences.Converter<T> {
        @Suppress("UNCHECKED_CAST")
        return converters[type] as Preferences.Converter<T>? ?: throw IllegalStateException("No registered converter for $type")
    }

    private inner class MutablePreferencesImpl(
        private val editor: SharedPreferences.Editor
    ) : PreferencesImpl(sharedPreferences, converters), MutablePreferences {

        override fun putBoolean(key: String, value: Boolean) {
            editor.putBoolean(key, value)
        }

        override fun putInt(key: String, value: Int) {
            editor.putInt(key, value)
        }

        override fun putLong(key: String, value: Long) {
            editor.putLong(key, value)
        }

        override fun putFloat(key: String, value: Float) {
            editor.putFloat(key, value)
        }

        override fun putString(key: String, value: String) {
            editor.putString(key, value)
        }

        override fun <T : Any> put(key: String, value: T) {
            val converter = getConverter<T>(value.javaClass)
            editor.putString(key, converter.toString(value))
        }

        override fun remove(key: String) {
            editor.remove(key)
        }

        override fun clear() {
            editor.clear()
        }

        override fun edit(action: MutablePreferences.() -> Unit) {
            action(this)
        }
    }

    private inner class ObservablePreferencesImpl : ObservablePreferences {
        private val keyChanges: Observable<String>

        init {
            keyChanges = Observable.create<String> { emitter ->
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key -> emitter.onNext(key) }
                sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
                emitter.setCancellable { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
            }.share()
        }

        override fun getBoolean(key: String, defaultValue: Boolean): Observable<Boolean> {
            return getValueObservable(key) { sharedPreferences.getBoolean(key, defaultValue) }
        }

        override fun getInt(key: String, defaultValue: Int): Observable<Int> {
            return getValueObservable(key) { sharedPreferences.getInt(key, defaultValue) }
        }

        override fun getLong(key: String, defaultValue: Long): Observable<Long> {
            return getValueObservable(key) { sharedPreferences.getLong(key, defaultValue) }
        }

        override fun getFloat(key: String, defaultValue: Float): Observable<Float> {
            return getValueObservable(key) { sharedPreferences.getFloat(key, defaultValue) }
        }

        override fun getString(key: String, defaultValue: String): Observable<String> {
            return getValueObservable(key) { sharedPreferences.getString(key, defaultValue) }
        }

        override fun <T : Any> get(key: String, defaultValue: T): Observable<T> {
            val converter = getConverter<T>(defaultValue.javaClass)
            return getValueObservable(key) {
                val stringValue: String? = sharedPreferences.getString(key, null)
                if (stringValue != null) converter.fromString(stringValue, defaultValue) else defaultValue
            }
        }

        private fun <T : Any> getValueObservable(key: String, reader: () -> T): Observable<T> {
            return keyChanges
                    .filter { it == key }
                    .startWith(key)
                    .map { reader() }
                    .distinctUntilChanged()
        }
    }
}
