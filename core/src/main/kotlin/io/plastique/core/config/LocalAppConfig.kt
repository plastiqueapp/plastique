package io.plastique.core.config

import android.content.Context
import androidx.annotation.XmlRes
import org.xmlpull.v1.XmlPullParser

class LocalAppConfig(context: Context, @XmlRes resourceId: Int) : AppConfig {
    private val configValues: Map<String, String> by lazy(LazyThreadSafetyMode.NONE) {
        context.resources.getXml(resourceId).use { readConfigValues(it) }
    }

    override fun getBoolean(key: String): Boolean {
        return configValues[key]?.toBoolean() ?: false
    }

    override fun getLong(key: String): Long {
        return configValues[key]?.toLong() ?: 0
    }

    override fun getString(key: String): String {
        return configValues[key] ?: ""
    }

    override fun fetch() {
        // No-op
    }

    private fun readConfigValues(xml: XmlPullParser): Map<String, String> {
        val configValues = linkedMapOf<String, String>()

        var tag: String? = null
        var key: String? = null
        var value: String? = null

        var eventType = xml.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> tag = xml.name

                XmlPullParser.END_TAG -> {
                    if (xml.name == "entry") {
                        if (key == null) {
                            throw IllegalStateException("Entry key is null")
                        }
                        if (value == null) {
                            throw IllegalStateException("Entry value is null")
                        }
                        configValues[key] = value
                        key = null
                        value = null
                    }
                    tag = null
                }

                XmlPullParser.TEXT -> {
                    when (tag) {
                        "key" -> key = xml.text
                        "value" -> value = xml.text
                    }
                }
            }
            eventType = xml.next()
        }
        return configValues
    }
}
