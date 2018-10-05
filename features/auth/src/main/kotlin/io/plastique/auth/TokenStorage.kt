package io.plastique.auth

import android.content.SharedPreferences
import javax.inject.Inject

class TokenStorage @Inject constructor(private val preferences: SharedPreferences) {
    val accessToken: String?
        get() = preferences.getString(PREF_ACCESS_TOKEN, null)

    val refreshToken: String?
        get() = preferences.getString(PREF_REFRESH_TOKEN, null)

    fun saveTokens(accessToken: String, refreshToken: String?) {
        preferences.edit()
                .putString(PREF_ACCESS_TOKEN, accessToken)
                .putString(PREF_REFRESH_TOKEN, refreshToken)
                .apply()
    }

    fun clear() {
        preferences.edit()
                .remove(PREF_ACCESS_TOKEN)
                .remove(PREF_REFRESH_TOKEN)
                .apply()
    }

    companion object {
        private const val PREF_ACCESS_TOKEN = "auth.access_token"
        private const val PREF_REFRESH_TOKEN = "auth.refresh_token"
    }
}
