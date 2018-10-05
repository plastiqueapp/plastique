package io.plastique.core.config

interface AppConfig {
    fun getBoolean(key: String): Boolean

    fun getLong(key: String): Long

    fun getString(key: String): String

    fun fetch()
}
