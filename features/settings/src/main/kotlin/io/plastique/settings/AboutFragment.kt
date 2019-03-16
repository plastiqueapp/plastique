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
import io.plastique.core.browser.BrowserLauncher
import io.plastique.core.config.AppConfig
import io.plastique.core.navigation.navigationContext
import io.plastique.inject.getComponent
import io.plastique.settings.licenses.LicensesActivity
import io.plastique.util.Intents
import io.plastique.util.VersionNumberComparator
import java.util.regex.Pattern
import javax.inject.Inject

class AboutFragment : BasePreferenceFragment(), Preference.OnPreferenceClickListener {
    @Inject lateinit var appConfig: AppConfig
    @Inject lateinit var browserLauncher: BrowserLauncher
    @Inject lateinit var navigator: SettingsNavigator

    private var newVersionAvailable: Boolean = false

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
            val a = context.obtainStyledAttributes(intArrayOf(R.attr.colorAccent))
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

    override fun onPreferenceClick(preference: Preference): Boolean {
        val context = requireContext()
        return when (preference.key) {
            "rate_app" -> {
                openPlayStore(context)
                true
            }
            "send_feedback" -> {
                val intent = Intents.sendEmail(
                        to = arrayOf(appConfig.getString("feedback_email")),
                        subject = appConfig.getString("feedback_subject"),
                        title = getString(R.string.about_send_email_title))
                startActivity(intent)
                true
            }
            "privacy_policy" -> {
                browserLauncher.openUrl(context, appConfig.getString("privacy_policy_url"))
                true
            }
            "deviantart_privacy_policy" -> {
                browserLauncher.openUrl(context, appConfig.getString("deviantart_privacy_policy_url"))
                true
            }
            "deviantart_tos" -> {
                browserLauncher.openUrl(context, appConfig.getString("deviantart_terms_of_service_url"))
                true
            }
            "licenses" -> {
                startActivity(LicensesActivity.createIntent(context))
                true
            }
            "app_version" -> {
                if (newVersionAvailable) {
                    openPlayStore(context)
                }
                true
            }
            else -> false
        }
    }

    private fun isNewVersionAvailable(currentVersionName: String): Boolean {
        val currentAppVersionWithoutSuffix = DEV_VERSION_NAME_SUFFIX.matcher(currentVersionName).replaceFirst("")
        val latestAppVersion = appConfig.getString("app_latest_version")
        return !latestAppVersion.isEmpty() && VersionNumberComparator().compare(currentAppVersionWithoutSuffix, latestAppVersion) < 0
    }

    private fun openPlayStore(context: Context) {
        val packageName = DEV_PACKAGE_NAME_SUFFIX.matcher(context.packageName).replaceFirst("")
        navigator.openPlayStore(navigationContext, packageName)
    }

    override fun injectDependencies() {
        getComponent<SettingsFragmentComponent>().inject(this)
    }

    companion object {
        private val DEV_PACKAGE_NAME_SUFFIX = Pattern.compile("\\.dev$")
        private val DEV_VERSION_NAME_SUFFIX = Pattern.compile("-dev$")
    }
}
