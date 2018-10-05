package io.plastique.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import javax.inject.Inject

class FileDownloader @Inject constructor(context: Context) {
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    fun downloadPicture(uri: Uri) {
        val fileName = guessFileName(uri)
        val request = DownloadManager.Request(uri).apply {
            setTitle(fileName)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, fileName)
            allowScanningByMediaScanner()
        }
        downloadManager.enqueue(request)
    }

    private fun guessFileName(uri: Uri): String {
        return uri.lastPathSegment?.takeIf { it.isNotEmpty() } ?: "downloadfile"
    }
}
