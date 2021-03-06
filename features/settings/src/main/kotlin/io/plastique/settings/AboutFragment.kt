package io.plastique.settings

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.content.pm.PackageInfoCompat
import androidx.preference.Preference
import androidx.preference.PreferenceGroup
import io.plastique.core.config.AppConfig
import io.plastique.core.navigation.Route
import io.plastique.core.navigation.navigationContext
import io.plastique.inject.getComponent
import io.plastique.util.Intents
import io.plastique.util.VersionNumberComparator
import java.util.regex.Pattern
import javax.inject.Inject

class AboutFragment : BasePreferenceFragment(), Preference.OnPreferenceClickListener {
    @Inject lateinit var appConfig: AppConfig
    @Inject lateinit var navigator: SettingsNavigator

    private var newVersionAvailable: Boolean = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigator.attach(navigationContext)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_about, rootKey)

        preferenceScreen.forEach { preference ->
            if (preference !is PreferenceGroup) {
                preference.onPreferenceClickListener = this
            }
        }

        val context = requireContext()
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val currentAppVersion = packageInfo.versionName + " (" + PackageInfoCompat.getLongVersionCode(packageInfo) + ")"
        newVersionAvailable = isNewVersionAvailable(packageInfo.versionName)

        val versionPreference = findPreference<Preference>("app_version")!!
        versionPreference.summary = if (newVersionAvailable) {
            val a = context.obtainStyledAttributes(intArrayOf(R.attr.colorPrimary))
            val color = a.getColor(0, Color.BLACK)
            a.recycle()

            val noticeText = SpannableString.valueOf(getString(R.string.about_new_version_available))
            noticeText.setSpan(ForegroundColorSpan(color), 0, noticeText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            SpannableStringBuilder().apply {
                append(currentAppVersion)
                append('\u2002')
                append(noticeText)
            }
        } else {
            currentAppVersion
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean = when (preference.key) {
        "rate_app" -> {
            openPlayStore(requireContext())
            true
        }
        "send_feedback" -> {
            sendFeedback()
            true
        }
        "privacy_policy" -> {
            navigator.openUrl(appConfig.getString("privacy_policy_url"))
            true
        }
        "deviantart_privacy_policy" -> {
            navigator.openUrl(appConfig.getString("deviantart_privacy_policy_url"))
            true
        }
        "deviantart_tos" -> {
            navigator.openUrl(appConfig.getString("deviantart_terms_of_service_url"))
            true
        }
        "licenses" -> {
            navigator.openLicenses()
            true
        }
        "app_version" -> {
            if (newVersionAvailable) {
                openPlayStore(requireContext())
            }
            true
        }
        else -> false
    }

    private fun sendFeedback() {
        val route = Route.Activity(Intents.sendEmail(
            to = arrayOf(appConfig.getString("feedback_email")),
            subject = appConfig.getString("feedback_subject"),
            title = getString(R.string.about_send_email_title)))
        navigator.navigateTo(route)
    }

    private fun openPlayStore(context: Context) {
        val packageName = DEV_PACKAGE_NAME_SUFFIX.matcher(context.packageName).replaceFirst("")
        navigator.openPlayStore(packageName)
    }

    private fun isNewVersionAvailable(currentVersionName: String): Boolean {
        val currentAppVersionWithoutSuffix = DEV_VERSION_NAME_SUFFIX.matcher(currentVersionName).replaceFirst("")
        val latestAppVersion = appConfig.getString("app_latest_version")
        return latestAppVersion.isNotEmpty() && VersionNumberComparator().compare(currentAppVersionWithoutSuffix, latestAppVersion) < 0
    }

    override fun injectDependencies() {
        getComponent<SettingsFragmentComponent>().inject(this)
    }

    companion object {
        private val DEV_PACKAGE_NAME_SUFFIX = Pattern.compile("\\.dev$")
        private val DEV_VERSION_NAME_SUFFIX = Pattern.compile("-dev$")
    }
}
