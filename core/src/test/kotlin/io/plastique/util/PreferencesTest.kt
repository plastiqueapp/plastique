package io.plastique.util

import android.preference.PreferenceManager
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
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
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application)
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
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application)
        preferences = Preferences.Builder()
                .sharedPreferences(sharedPreferences)
                .addConverter(Foo::class.java, FooConverter)
                .build()
    }

    @Test
    fun `getBoolean emits current value on subscribe if preference exists`() {
        preferences.edit { putBoolean("bool", true) }

        val ts = preferences.observable().getBoolean("bool", false).test()
        ts.assertValues(true)
        ts.dispose()
    }

    @Test
    fun `getBoolean emits default value on subscribe if preference doesnt exist`() {
        val ts = preferences.observable().getBoolean("bool", true).test()
        ts.assertValues(true)
        ts.dispose()
    }

    @Test
    fun `getBoolean emits new value after preference was changed`() {
        val ts = preferences.observable().getBoolean("bool", false).test()
        preferences.edit { putBoolean("bool", true) }
        ts.assertValues(false, true)
        ts.dispose()
    }

    @Test
    fun `getBoolean emits default value after preference was removed`() {
        preferences.edit { putBoolean("bool", true) }

        val ts = preferences.observable().getBoolean("bool", false).test()
        preferences.edit { remove("bool") }
        ts.assertValues(true, false)
        ts.dispose()
    }

    @Test
    fun `getBoolean doesnt emit after preference was changed if value equals to previous`() {
        val ts = preferences.observable().getBoolean("bool", false).test()
        preferences.edit { putBoolean("bool", false) }
        ts.assertValues(false)
        ts.dispose()
    }

    @Test
    fun `getInt emits current value on subscribe if preference exists`() {
        preferences.edit { putInt("int", 42) }

        val ts = preferences.observable().getInt("int", 1).test()
        ts.assertValues(42)
        ts.dispose()
    }

    @Test
    fun `getInt emits default value on subscribe if preference doesnt exist`() {
        val ts = preferences.observable().getInt("int", 1).test()
        ts.assertValues(1)
        ts.dispose()
    }

    @Test
    fun `getInt emits new value after preference was changed`() {
        val ts = preferences.observable().getInt("int", 1).test()
        preferences.edit { putInt("int", 2) }
        ts.assertValues(1, 2)
        ts.dispose()
    }

    @Test
    fun `getInt emits default value after preference was removed`() {
        preferences.edit { putInt("int", 1) }

        val ts = preferences.observable().getInt("int", 2).test()
        preferences.edit { remove("int") }
        ts.assertValues(1, 2)
        ts.dispose()
    }

    @Test
    fun `getInt doesnt emit after preference was changed if value equals to previous`() {
        val ts = preferences.observable().getInt("int", 1).test()
        preferences.edit { putInt("int", 1) }
        ts.assertValues(1)
        ts.dispose()
    }

    @Test
    fun `getLong emits current value on subscribe if preference exists`() {
        preferences.edit { putLong("long", 42) }

        val ts = preferences.observable().getLong("long", 1).test()
        ts.assertValues(42L)
        ts.dispose()
    }

    @Test
    fun `getLong emits default value on subscribe if preference doesnt exist`() {
        val ts = preferences.observable().getLong("long", 1).test()
        ts.assertValues(1L)
        ts.dispose()
    }

    @Test
    fun `getLong emits new value after preference was changed`() {
        val ts = preferences.observable().getLong("long", 1).test()
        preferences.edit { putLong("long", 2) }
        ts.assertValues(1L, 2L)
        ts.dispose()
    }

    @Test
    fun `getLong emits default value after preference was removed`() {
        preferences.edit { putLong("long", 1) }

        val ts = preferences.observable().getLong("long", 2).test()
        preferences.edit { remove("long") }
        ts.assertValues(1L, 2L)
        ts.dispose()
    }

    @Test
    fun `getLong doesnt emit after preference was changed if value equals to previous`() {
        val ts = preferences.observable().getLong("long", 1).test()
        preferences.edit { putLong("long", 1) }
        ts.assertValues(1L)
        ts.dispose()
    }

    @Test
    fun `getFloat emits current value on subscribe if preference exists`() {
        preferences.edit { putFloat("float", 42.0f) }

        val ts = preferences.observable().getFloat("float", 1.0f).test()
        ts.assertValues(42.0f)
        ts.dispose()
    }

    @Test
    fun `getFloat emits default value on subscribe if preference doesnt exist`() {
        val ts = preferences.observable().getFloat("float", 1.0f).test()
        ts.assertValues(1.0f)
        ts.dispose()
    }

    @Test
    fun `getFloat emits new value after preference was changed`() {
        val ts = preferences.observable().getFloat("float", 1.0f).test()
        preferences.edit { putFloat("float", 2.0f) }
        ts.assertValues(1.0f, 2.0f)
        ts.dispose()
    }

    @Test
    fun `getFloat emits default value after preference was removed`() {
        preferences.edit { putFloat("float", 1.0f) }

        val ts = preferences.observable().getFloat("float", 2.0f).test()
        preferences.edit { remove("float") }
        ts.assertValues(1.0f, 2.0f)
        ts.dispose()
    }

    @Test
    fun `getFloat doesnt emit after preference was changed if value equals to previous`() {
        val ts = preferences.observable().getFloat("float", 1.0f).test()
        preferences.edit { putFloat("float", 1.0f) }
        ts.assertValues(1.0f)
        ts.dispose()
    }

    @Test
    fun `getString emits current value on subscribe if preference exists`() {
        preferences.edit { putString("string", "bar") }

        val ts = preferences.observable().getString("string", "foo").test()
        ts.assertValues("bar")
        ts.dispose()
    }

    @Test
    fun `getString emits default value on subscribe if preference doesnt exist`() {
        val ts = preferences.observable().getString("string", "foo").test()
        ts.assertValues("foo")
        ts.dispose()
    }

    @Test
    fun `getString emits new value after preference was changed`() {
        val ts = preferences.observable().getString("string", "foo").test()
        preferences.edit { putString("string", "bar") }
        ts.assertValues("foo", "bar")
        ts.dispose()
    }

    @Test
    fun `getString emits default value after preference was removed`() {
        preferences.edit { putString("string", "bar") }

        val ts = preferences.observable().getString("string", "foo").test()
        preferences.edit { remove("string") }
        ts.assertValues("bar", "foo")
        ts.dispose()
    }

    @Test
    fun `getString doesnt emit after preference was changed if value equals to previous`() {
        val ts = preferences.observable().getString("string", "foo").test()
        preferences.edit { putString("string", "foo") }
        ts.assertValues("foo")
        ts.dispose()
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

        val ts = preferences.observable().get("custom", A).test()
        ts.assertValues(B)
        ts.dispose()
    }

    @Test
    fun `get emits default value on subscribe if preference doesnt exist`() {
        val ts = preferences.observable().get("custom", A).test()
        ts.assertValues(A)
        ts.dispose()
    }

    @Test
    fun `get emits new value after preference was changed`() {
        val ts = preferences.observable().get("custom", A).test()
        preferences.edit { put("custom", B) }
        ts.assertValues(A, B)
        ts.dispose()
    }

    @Test
    fun `get emits default value after preference was removed`() {
        preferences.edit { put("custom", B) }

        val ts = preferences.observable().get("custom", A).test()
        preferences.edit { remove("custom") }
        ts.assertValues(B, A)
        ts.dispose()
    }

    @Test
    fun `get doesn't emit after preference was changed if value is equal to previous`() {
        val ts = preferences.observable().get("custom", A).test()
        preferences.edit { put("custom", A) }
        ts.assertValues(A)
        ts.dispose()
    }
}

private data class Foo(val value: String)

private object FooConverter : Preferences.Converter<Foo> {
    override fun fromString(string: String, defaultValue: Foo): Foo = Foo(string)

    override fun toString(value: Foo): String = value.value
}

private val A = Foo("a")
private val B = Foo("b")
