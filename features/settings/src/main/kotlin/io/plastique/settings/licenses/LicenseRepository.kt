package io.plastique.settings.licenses

import android.content.Context
import com.squareup.moshi.Moshi
import io.plastique.core.json.adapter
import io.reactivex.Single
import okio.buffer
import okio.source
import javax.inject.Inject

class LicenseRepository @Inject constructor(
    private val context: Context,
    private val moshi: Moshi
) {
    fun getLicenses(): Single<List<License>> {
        return Single.fromCallable(::getLicensesInternal)
    }

    private fun getLicensesInternal(): List<License> {
        return context.assets.open(LICENSES_FILE_NAME).source().buffer().use { source ->
            moshi.adapter<List<License>>().fromJson(source)!!
        }
    }
}

private const val LICENSES_FILE_NAME = "licenses.json"
