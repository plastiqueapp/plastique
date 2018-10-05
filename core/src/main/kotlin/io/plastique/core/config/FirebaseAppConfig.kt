package io.plastique.core.config

import androidx.annotation.XmlRes
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import io.plastique.core.BuildConfig
import java.util.concurrent.TimeUnit

class FirebaseAppConfig(@XmlRes defaultsResId: Int) : AppConfig {
    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    init {
        remoteConfig.setConfigSettings(FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build())
        remoteConfig.setDefaults(defaultsResId)
    }

    override fun getBoolean(key: String): Boolean {
        return remoteConfig.getBoolean(key)
    }

    override fun getLong(key: String): Long {
        return remoteConfig.getLong(key)
    }

    override fun getString(key: String): String {
        return remoteConfig.getString(key)
    }

    override fun fetch() {
        remoteConfig.activateFetched()
        remoteConfig.fetch(if (BuildConfig.DEBUG) {
            TimeUnit.MINUTES.toSeconds(1)
        } else {
            TimeUnit.HOURS.toSeconds(12)
        })
    }
}
