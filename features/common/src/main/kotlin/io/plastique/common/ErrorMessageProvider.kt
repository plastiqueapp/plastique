package io.plastique.common

import android.content.Context
import android.text.TextUtils
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import io.plastique.core.content.EmptyState
import io.plastique.core.network.NoNetworkConnectionException
import io.plastique.core.session.AuthenticationExpiredException
import io.plastique.deviations.DeviationNotFoundException
import io.plastique.users.UserNotFoundException
import javax.inject.Inject

class ErrorMessageProvider @Inject constructor(
    private val context: Context
) {
    fun getErrorMessage(error: Throwable, @StringRes defaultMessageId: Int = R.string.common_message_load_failed): String = when (error) {
        is NoNetworkConnectionException -> context.getString(R.string.common_message_no_internet_connection)
        is AuthenticationExpiredException -> context.getString(R.string.common_message_authentication_expired)
        else -> context.getString(defaultMessageId)
    }

    fun getErrorState(error: Throwable, @StringRes defaultMessageId: Int = R.string.common_message_load_failed): EmptyState = when (error) {
        is NoNetworkConnectionException -> EmptyState.MessageWithButton(
            message = context.getString(R.string.common_message_no_internet_connection),
            button = context.getString(R.string.common_button_try_again))

        is UserNotFoundException -> EmptyState.Message(
            message = HtmlCompat.fromHtml(context.getString(R.string.common_message_user_not_found, TextUtils.htmlEncode(error.username)), 0))

        is DeviationNotFoundException -> EmptyState.Message(
            message = context.getString(R.string.common_message_deviation_not_found))

        is AuthenticationExpiredException -> EmptyState.MessageWithButton(
            message = context.getString(R.string.common_message_authentication_expired),
            button = context.getString(R.string.common_button_try_again))

        else -> EmptyState.MessageWithButton(
            message = context.getString(defaultMessageId),
            button = context.getString(R.string.common_button_try_again))
    }
}
