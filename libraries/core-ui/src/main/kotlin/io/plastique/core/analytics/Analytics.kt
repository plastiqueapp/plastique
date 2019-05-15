package io.plastique.core.analytics

import android.content.Context
import android.content.pm.PackageManager
import com.google.android.gms.common.GoogleApiAvailability
import io.plastique.core.ui.R
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Analytics @Inject constructor(
    private val context: Context,
    private val trackers: List<@JvmSuppressWildcards Tracker>
) {

    fun setUserProperty(name: String, value: String?) {
        trackers.forEach { tracker -> tracker.setUserProperty(name, value) }
    }

    fun initUserProperties() {
        setUserProperty(UserProperties.PLAY_SERVICES_VERSION, getPlayServicesVersion())
    }

    fun initDatabaseSize(dbFile: File) {
        setUserProperty(UserProperties.DATABASE_SIZE, getDatabaseSize(dbFile))
    }

    private fun getPlayServicesVersion(): String {
        val packageInfo = try {
            context.packageManager.getPackageInfo(GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            return "none"
        }

        val matcher = Pattern.compile("^(\\d+\\.\\d+)[^\\d]").matcher(packageInfo.versionName)
        return if (matcher.find()) {
            matcher.group(1)
        } else {
            "unknown"
        }
    }

    private fun getDatabaseSize(file: File): String {
        val size = BigDecimal(file.length()).divide(BYTES_IN_MIB, 1, RoundingMode.HALF_UP)
        return context.getString(R.string.analytics_property_database_size, size.toPlainString())
    }

    companion object {
        private val BYTES_IN_MIB = BigDecimal(1024 * 1024)
    }
}
