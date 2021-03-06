package io.plastique.util

import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

class PreferencesBuilderTest {
    @Test
    fun `build throws IllegalStateException if SharedPreferences was not set`() {
        val exception = assertThrows<IllegalStateException> {
            Preferences.Builder().build()
        }
        assertEquals("SharedPreferences is required", exception.message)
    }
}

@RunWith(RobolectricTestRunner::class)
class MutablePreferencesTest {
    private lateinit var preferences: Preferences

    @Before
    fun setUp() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
        preferences = Preferences.Builder()
            .sharedPreferences(sharedPreferences)
            .addConverter(Foo::class.java, FooConverter)
            .build()
    }

    @Test
    fun putBoolean() {
        preferences.edit { putBoolean("bool", true) }
        assertEquals(true, preferences.getBoolean("bool", false))
    }

    @Test
    fun putInt() {
        preferences.edit { putInt("int", 2) }
        assertEquals(2, preferences.getInt("int", 1))
    }

    @Test
    fun putLong() {
        preferences.edit { putLong("long", 2L) }
        assertEquals(2, preferences.getLong("long", 1))
    }

    @Test
    fun putFloat() {
        preferences.edit { putFloat("float", 2.0f) }
        assertEquals(2.0f, preferences.getFloat("float", 1.0f))
    }

    @Test
    fun putString() {
        preferences.edit { putString("string", "bar") }
        assertEquals("bar", preferences.getString("string", "foo"))
    }

    @Test
    fun put() {
        preferences.edit { put("custom", B) }
        assertEquals(B, preferences.get("custom", A))
    }

    @Test
    fun `put throws IllegalStateException if converter for the class is not registered`() {
        val exception = assertThrows<IllegalStateException> {
            preferences.edit { put("date", Date()) }
        }
        assertEquals("No registered converter for class java.util.Date", exception.message)
    }

    @Test
    fun remove() {
        preferences.edit { putInt("int", 2) }
        preferences.edit { remove("int") }
        assertEquals(1, preferences.getInt("int", 1))
    }

    @Test
    fun clear() {
        preferences.edit { putInt("int", 2) }
        preferences.edit { clear() }
        assertEquals(1, preferences.getInt("int", 1))
    }
}

@RunWith(RobolectricTestRunner::class)
class ObservablePreferencesTest {
    private lateinit var preferences: Preferences

    @Before
    fun setUp() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
        preferences = Preferences.Builder()
            .sharedPreferences(sharedPreferences)
            .addConverter(Foo::class.java, FooConverter)
            .build()
    }

    @Test
    fun `getBoolean emits current value on subscribe if preference exists`() {
        preferences.edit { putBoolean("bool", true) }

        val observer = preferences.observable().getBoolean("bool", false).test()
        observer.assertValues(true)
        observer.dispose()
    }

    @Test
    fun `getBoolean emits default value on subscribe if preference doesn't exist`() {
        val observer = preferences.observable().getBoolean("bool", true).test()
        observer.assertValues(true)
        observer.dispose()
    }

    @Test
    fun `getBoolean emits new value after preference was changed`() {
        val observer = preferences.observable().getBoolean("bool", false).test()
        preferences.edit { putBoolean("bool", true) }
        observer.assertValues(false, true)
        observer.dispose()
    }

    @Test
    fun `getBoolean emits default value after preference was removed`() {
        preferences.edit { putBoolean("bool", true) }

        val observer = preferences.observable().getBoolean("bool", false).test()
        preferences.edit { remove("bool") }
        observer.assertValues(true, false)
        observer.dispose()
    }

    @Test
    fun `getBoolean doesn't emit after preference was changed if value equals to previous`() {
        val observer = preferences.observable().getBoolean("bool", false).test()
        preferences.edit { putBoolean("bool", false) }
        observer.assertValues(false)
        observer.dispose()
    }

    @Test
    fun `getInt emits current value on subscribe if preference exists`() {
        preferences.edit { putInt("int", 42) }

        val observer = preferences.observable().getInt("int", 1).test()
        observer.assertValues(42)
        observer.dispose()
    }

    @Test
    fun `getInt emits default value on subscribe if preference doesn't exist`() {
        val observer = preferences.observable().getInt("int", 1).test()
        observer.assertValues(1)
        observer.dispose()
    }

    @Test
    fun `getInt emits new value after preference was changed`() {
        val observer = preferences.observable().getInt("int", 1).test()
        preferences.edit { putInt("int", 2) }
        observer.assertValues(1, 2)
        observer.dispose()
    }

    @Test
    fun `getInt emits default value after preference was removed`() {
        preferences.edit { putInt("int", 1) }

        val observer = preferences.observable().getInt("int", 2).test()
        preferences.edit { remove("int") }
        observer.assertValues(1, 2)
        observer.dispose()
    }

    @Test
    fun `getInt doesn't emit after preference was changed if value equals to previous`() {
        val observer = preferences.observable().getInt("int", 1).test()
        preferences.edit { putInt("int", 1) }
        observer.assertValues(1)
        observer.dispose()
    }

    @Test
    fun `getLong emits current value on subscribe if preference exists`() {
        preferences.edit { putLong("long", 42) }

        val observer = preferences.observable().getLong("long", 1).test()
        observer.assertValues(42L)
        observer.dispose()
    }

    @Test
    fun `getLong emits default value on subscribe if preference doesn't exist`() {
        val observer = preferences.observable().getLong("long", 1).test()
        observer.assertValues(1L)
        observer.dispose()
    }

    @Test
    fun `getLong emits new value after preference was changed`() {
        val observer = preferences.observable().getLong("long", 1).test()
        preferences.edit { putLong("long", 2) }
        observer.assertValues(1L, 2L)
        observer.dispose()
    }

    @Test
    fun `getLong emits default value after preference was removed`() {
        preferences.edit { putLong("long", 1) }

        val observer = preferences.observable().getLong("long", 2).test()
        preferences.edit { remove("long") }
        observer.assertValues(1L, 2L)
        observer.dispose()
    }

    @Test
    fun `getLong doesn't emit after preference was changed if value equals to previous`() {
        val observer = preferences.observable().getLong("long", 1).test()
        preferences.edit { putLong("long", 1) }
        observer.assertValues(1L)
        observer.dispose()
    }

    @Test
    fun `getFloat emits current value on subscribe if preference exists`() {
        preferences.edit { putFloat("float", 42.0f) }

        val observer = preferences.observable().getFloat("float", 1.0f).test()
        observer.assertValues(42.0f)
        observer.dispose()
    }

    @Test
    fun `getFloat emits default value on subscribe if preference doesn't exist`() {
        val tobserver = preferences.observable().getFloat("float", 1.0f).test()
        tobserver.assertValues(1.0f)
        tobserver.dispose()
    }

    @Test
    fun `getFloat emits new value after preference was changed`() {
        val observer = preferences.observable().getFloat("float", 1.0f).test()
        preferences.edit { putFloat("float", 2.0f) }
        observer.assertValues(1.0f, 2.0f)
        observer.dispose()
    }

    @Test
    fun `getFloat emits default value after preference was removed`() {
        preferences.edit { putFloat("float", 1.0f) }

        val observer = preferences.observable().getFloat("float", 2.0f).test()
        preferences.edit { remove("float") }
        observer.assertValues(1.0f, 2.0f)
        observer.dispose()
    }

    @Test
    fun `getFloat doesn't emit after preference was changed if value equals to previous`() {
        val observer = preferences.observable().getFloat("float", 1.0f).test()
        preferences.edit { putFloat("float", 1.0f) }
        observer.assertValues(1.0f)
        observer.dispose()
    }

    @Test
    fun `getString emits current value on subscribe if preference exists`() {
        preferences.edit { putString("string", "bar") }

        val observer = preferences.observable().getString("string", "foo").test()
        observer.assertValues("bar")
        observer.dispose()
    }

    @Test
    fun `getString emits default value on subscribe if preference doesn't exist`() {
        val observer = preferences.observable().getString("string", "foo").test()
        observer.assertValues("foo")
        observer.dispose()
    }

    @Test
    fun `getString emits new value after preference was changed`() {
        val observer = preferences.observable().getString("string", "foo").test()
        preferences.edit { putString("string", "bar") }
        observer.assertValues("foo", "bar")
        observer.dispose()
    }

    @Test
    fun `getString emits default value after preference was removed`() {
        preferences.edit { putString("string", "bar") }

        val observer = preferences.observable().getString("string", "foo").test()
        preferences.edit { remove("string") }
        observer.assertValues("bar", "foo")
        observer.dispose()
    }

    @Test
    fun `getString doesn't emit after preference was changed if value equals to previous`() {
        val observer = preferences.observable().getString("string", "foo").test()
        preferences.edit { putString("string", "foo") }
        observer.assertValues("foo")
        observer.dispose()
    }

    @Test
    fun `get throws IllegalStateException if converter for the class is not registered`() {
        val exception = assertThrows<IllegalStateException> {
            preferences.observable().get("date", Date())
        }
        assertEquals("No registered converter for class java.util.Date", exception.message)
    }

    @Test
    fun `get emits current value on subscribe if preference exists`() {
        preferences.edit { put("custom", B) }

        val observer = preferences.observable().get("custom", A).test()
        observer.assertValues(B)
        observer.dispose()
    }

    @Test
    fun `get emits default value on subscribe if preference doesn't exist`() {
        val observer = preferences.observable().get("custom", A).test()
        observer.assertValues(A)
        observer.dispose()
    }

    @Test
    fun `get emits new value after preference was changed`() {
        val observer = preferences.observable().get("custom", A).test()
        preferences.edit { put("custom", B) }
        observer.assertValues(A, B)
        observer.dispose()
    }

    @Test
    fun `get emits default value after preference was removed`() {
        preferences.edit { put("custom", B) }

        val observer = preferences.observable().get("custom", A).test()
        preferences.edit { remove("custom") }
        observer.assertValues(B, A)
        observer.dispose()
    }

    @Test
    fun `get doesn't emit after preference was changed if value is equal to previous`() {
        val observer = preferences.observable().get("custom", A).test()
        preferences.edit { put("custom", A) }
        observer.assertValues(A)
        observer.dispose()
    }
}

private data class Foo(val value: String)

private object FooConverter : Preferences.Converter<Foo> {
    override fun fromString(string: String, defaultValue: Foo): Foo = Foo(string)

    override fun toString(value: Foo): String = value.value
}

private val A = Foo("a")
private val B = Foo("b")
