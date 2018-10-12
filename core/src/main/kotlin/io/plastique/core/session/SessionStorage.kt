package io.plastique.core.session

import io.plastique.util.Preferences
import javax.inject.Inject

class SessionStorage @Inject constructor(private val preferences: Preferences) {
    fun getSession(): Session {
        val accessToken = preferences.getString(PREF_ACCESS_TOKEN)
        val refreshToken = preferences.getString(PREF_REFRESH_TOKEN)
        val userId = preferences.getString(PREF_USER_ID)
        val username = preferences.getString(PREF_USERNAME)
        return when {
            accessToken != null && refreshToken != null && userId != null && username != null ->
                Session.User(accessToken = accessToken, refreshToken = refreshToken, userId = userId, username = username)
            accessToken != null && refreshToken == null ->
                Session.Anonymous(accessToken = accessToken)
            else -> Session.None
        }
    }

    fun saveSession(session: Session) {
        preferences.edit {
            when (session) {
                is Session.Anonymous -> {
                    putString(PREF_ACCESS_TOKEN, session.accessToken)
                    remove(PREF_REFRESH_TOKEN)
                    remove(PREF_USER_ID)
                    remove(PREF_USERNAME)
                }
                is Session.User -> {
                    putString(PREF_ACCESS_TOKEN, session.accessToken)
                    putString(PREF_REFRESH_TOKEN, session.refreshToken)
                    putString(PREF_USER_ID, session.userId)
                    putString(PREF_USERNAME, session.username)
                }
                Session.None -> {
                    remove(PREF_ACCESS_TOKEN)
                    remove(PREF_REFRESH_TOKEN)
                    remove(PREF_USER_ID)
                    remove(PREF_USERNAME)
                }
            }
        }
    }

    companion object {
        private const val PREF_ACCESS_TOKEN = "session.access_token"
        private const val PREF_REFRESH_TOKEN = "session.refresh_token"
        private const val PREF_USER_ID = "session.user_id"
        private const val PREF_USERNAME = "session.username"
    }
}