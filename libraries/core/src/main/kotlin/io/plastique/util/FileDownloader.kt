package io.plastique.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import com.github.technoir42.android.extensions.requireSystemService
import javax.inject.Inject

class FileDownloader @Inject constructor(context: Context) {
    private val downloadManager = context.requireSystemService<DownloadManager>()

    fun downloadPicture(uri: Uri) {
        val fileName = guessFileName(uri)
        val request = DownloadManager.Request(uri).apply {
            setTitle(fileName)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, fileName)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                @Suppress("DEPRECATION")
                allowScanningByMediaScanner()
            }
        }
        downloadManager.enqueue(request)
    }

    private fun guessFileName(uri: Uri): String {
        return uri.lastPathSegment?.takeIf { it.isNotEmpty() } ?: "downloadfile"
    }
}
