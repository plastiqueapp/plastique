package io.plastique.util

import android.content.Context
import android.content.Intent
import android.net.MailTo
import android.net.Uri

object Intents {
    fun view(uri: Uri): Intent {
        return Intent(Intent.ACTION_VIEW, uri)
    }

    fun openPlayStore(context: Context, packageName: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            .setPackage("com.android.vending")
        if (context.packageManager.resolveActivity(intent, 0) != null) {
            return intent
        }
        return Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
    }

    fun sendEmail(to: Array<String>, subject: String = "", title: String? = null): Intent {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            type = "message/rfc822"
            data = Uri.parse(MailTo.MAILTO_SCHEME)
            putExtra(Intent.EXTRA_EMAIL, to)
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        return Intent.createChooser(intent, title)
    }
}
