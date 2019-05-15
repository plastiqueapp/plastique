package io.plastique.core.client

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import io.plastique.api.common.ErrorData
import io.plastique.core.exceptions.ApiException
import io.plastique.core.exceptions.HttpException
import io.plastique.core.exceptions.RateLimitExceededException
import io.plastique.util.adapter
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class ErrorResponseParser @Inject constructor(moshi: Moshi) {
    private val errorDataAdapter = moshi.adapter<ErrorData>()

    fun parse(response: Response<*>): HttpException {
        if (response.code() == HttpResponseCodes.TOO_MANY_REQUESTS) {
            return RateLimitExceededException(response.code(), response.raw().request().url().encodedPath())
        }
        val errorBody = response.errorBody()
        if (errorBody != null && isClientHttpError(response.code())) {
            try {
                val errorData = errorDataAdapter.fromJson(errorBody.source())
                return ApiException(response.code(), errorData!!)
            } catch (e: IOException) {
                Timber.e(e)
            } catch (e: JsonDataException) {
                Timber.e(e)
            }
        }
        return HttpException(response.code())
    }

    @Suppress("MagicNumber")
    private fun isClientHttpError(responseCode: Int): Boolean {
        return responseCode in 400..499
    }
}
