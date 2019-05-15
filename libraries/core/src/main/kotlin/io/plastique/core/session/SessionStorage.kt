package io.plastique.core.session

import io.plastique.util.Cryptor
import io.plastique.util.Preferences
import timber.log.Timber
import javax.inject.Inject

class SessionStorage @Inject constructor(private val preferences: Preferences, private val cryptor: Cryptor) {
    @Suppress("TooGenericExceptionCaught")
    fun getSession(): Session {
        val accessToken: String?
        val refreshToken: String?
        try {
            accessToken = preferences.getString(PREF_ACCESS_TOKEN)?.let { cryptor.decryptString(it, SECRET_KEY_ALIAS) }
            refreshToken = preferences.getString(PREF_REFRESH_TOKEN)?.let { cryptor.decryptString(it, SECRET_KEY_ALIAS) }
        } catch (e: Exception) {
            Timber.e(e, "Unable to restore session")
            return Session.None
        }
        val userId = preferences.getString(PREF_USER_ID)
        val username = preferences.getString(PREF_USERNAME)
        return when {
            accessToken != null && refreshToken != null && userId != null && username != null ->
                Session.User(accessToken = accessToken, refreshToken = refreshToken, userId = userId, username = username)
            accessToken != null && refreshToken == null && userId == null && username == null ->
                Session.Anonymous(accessToken = accessToken)
            else -> Session.None
        }
    }

    fun saveSession(session: Session) {
        preferences.edit {
            when (session) {
                is Session.Anonymous -> {
                    putString(PREF_ACCESS_TOKEN, cryptor.encryptString(session.accessToken, SECRET_KEY_ALIAS))
                    remove(PREF_REFRESH_TOKEN)
                    remove(PREF_USER_ID)
                    remove(PREF_USERNAME)
                }
                is Session.User -> {
                    putString(PREF_ACCESS_TOKEN, cryptor.encryptString(session.accessToken, SECRET_KEY_ALIAS))
                    putString(PREF_REFRESH_TOKEN, cryptor.encryptString(session.refreshToken, SECRET_KEY_ALIAS))
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
        private const val SECRET_KEY_ALIAS = "storage"
        private const val PREF_ACCESS_TOKEN = "session.access_token"
        private const val PREF_REFRESH_TOKEN = "session.refresh_token"
        private const val PREF_USER_ID = "session.user_id"
        private const val PREF_USERNAME = "session.username"
    }
}
