package io.plastique.common

import androidx.annotation.StringRes
import androidx.core.text.htmlEncode
import io.plastique.core.content.EmptyState
import io.plastique.core.network.NoNetworkConnectionException
import io.plastique.core.session.AuthenticationExpiredException
import io.plastique.deviations.DeviationNotFoundException
import io.plastique.users.UserNotFoundException
import javax.inject.Inject

class ErrorMessageProvider @Inject constructor() {
    @StringRes
    fun getErrorMessageId(error: Throwable, @StringRes defaultMessageId: Int = R.string.common_message_load_failed): Int = when (error) {
        is NoNetworkConnectionException -> R.string.common_message_no_internet_connection
        is AuthenticationExpiredException -> R.string.common_message_authentication_expired
        else -> defaultMessageId
    }

    fun getErrorState(error: Throwable, @StringRes defaultMessageId: Int = R.string.common_message_load_failed): EmptyState = when (error) {
        is NoNetworkConnectionException -> EmptyState.MessageWithButton(
            messageResId = R.string.common_message_no_internet_connection,
            buttonTextId = R.string.common_button_try_again)

        is UserNotFoundException -> EmptyState.Message(
            messageResId = R.string.common_message_user_not_found,
            messageArgs = listOf(error.username.htmlEncode()))

        is DeviationNotFoundException -> EmptyState.Message(
            messageResId = R.string.common_message_deviation_not_found)

        is AuthenticationExpiredException -> EmptyState.MessageWithButton(
            messageResId = R.string.common_message_authentication_expired,
            buttonTextId = R.string.common_button_try_again)

        else -> EmptyState.MessageWithButton(
            messageResId = defaultMessageId,
            buttonTextId = R.string.common_button_try_again)
    }
}
