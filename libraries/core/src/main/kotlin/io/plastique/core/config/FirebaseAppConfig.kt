package io.plastique.core.config

import androidx.annotation.XmlRes
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.threeten.bp.Duration

class FirebaseAppConfig(@XmlRes defaultsResId: Int, minFetchInterval: Duration) : AppConfig {
    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    init {
        remoteConfig.setConfigSettingsAsync(FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(minFetchInterval.seconds)
            .build())
        remoteConfig.setDefaultsAsync(defaultsResId)
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
        remoteConfig.activate()
        remoteConfig.fetch()
    }
}
