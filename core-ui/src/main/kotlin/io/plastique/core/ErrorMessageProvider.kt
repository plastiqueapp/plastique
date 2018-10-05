package io.plastique.core

import android.content.Context
import androidx.annotation.StringRes
import io.plastique.core.content.EmptyState
import io.plastique.core.exceptions.NoNetworkConnectionException
import io.plastique.core.ui.R
import javax.inject.Inject

class ErrorMessageProvider @Inject constructor(
    private val context: Context
) {
    fun getErrorMessage(error: Throwable, @StringRes defaultMessageId: Int = R.string.common_message_load_failed): String = when (error) {
        is NoNetworkConnectionException -> context.getString(R.string.common_message_no_internet_connection)
        else -> context.getString(defaultMessageId)
    }

    fun getErrorState(error: Throwable, @StringRes defaultMessageId: Int = R.string.common_message_load_failed): EmptyState = when (error) {
        is NoNetworkConnectionException -> EmptyState(
                message = context.getString(R.string.common_message_no_internet_connection),
                button = context.getString(R.string.common_button_try_again))
        else -> EmptyState(
                message = context.getString(defaultMessageId),
                button = context.getString(R.string.common_button_try_again))
    }
}
