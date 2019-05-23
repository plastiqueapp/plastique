package io.plastique.test.util

import android.os.Environment
import androidx.test.runner.screenshot.ScreenCapture
import androidx.test.runner.screenshot.ScreenCaptureProcessor
import androidx.test.runner.screenshot.Screenshot
import java.io.File
import java.io.IOException
import java.util.UUID

fun takeScreenshot(name: String) {
    val screenshot = Screenshot.capture()
    screenshot.name = name
    screenshot.process()
}

object ScreenshotProcessor : ScreenCaptureProcessor {
    override fun process(capture: ScreenCapture): String {
        val fileName = (capture.name ?: UUID.randomUUID().toString()) + "." + capture.format.name.toLowerCase()
        val outputFile = File(screenshotDir, fileName)

        outputFile.outputStream().buffered().use { output ->
            capture.bitmap.compress(capture.format, 100, output)
        }

        return outputFile.name
    }

    private val screenshotDir: File
        get() {
            val screenshotDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "screenshots/plastique")
            screenshotDir.mkdirs()

            if (!screenshotDir.isDirectory || !screenshotDir.canWrite()) {
                throw IOException("The directory $screenshotDir does not exist and could not be created or is not writable.")
            }
            return screenshotDir
        }
}
