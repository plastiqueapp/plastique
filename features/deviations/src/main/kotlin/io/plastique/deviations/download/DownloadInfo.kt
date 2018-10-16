package io.plastique.deviations.download

import io.plastique.util.Size

data class DownloadInfo(
    val downloadUrl: String,
    val size: Size,
    val fileSize: Int
)

fun DownloadInfoEntity.toDownloadInfo(): DownloadInfo =
        DownloadInfo(downloadUrl = downloadUrl, size = size, fileSize = fileSize)
