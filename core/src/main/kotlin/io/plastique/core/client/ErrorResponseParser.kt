package io.plastique.core.client

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import io.plastique.api.common.ErrorResponse
import io.plastique.core.exceptions.ApiException
import io.plastique.core.exceptions.ApiHttpException
import io.plastique.core.exceptions.ApiResponseException
import io.plastique.util.adapter
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class ErrorResponseParser @Inject constructor(
    private val moshi: Moshi
) {

    fun parse(response: Response<*>): ApiException {
        val errorBody = response.errorBody()
        if (isClientHttpError(response.code()) && errorBody != null) {
            try {
                val errorResponse = moshi.adapter<ErrorResponse>().fromJson(errorBody.source())
                return ApiResponseException(response, errorResponse!!)
            } catch (ignored: IOException) {
            } catch (e: JsonDataException) {
                Timber.e(e)
            }
        }
        return ApiHttpException(response)
    }

    private fun isClientHttpError(responseCode: Int): Boolean {
        return responseCode in 400..499
    }
}