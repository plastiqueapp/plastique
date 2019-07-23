package io.plastique.common

import io.plastique.core.client.NoNetworkConnectionException

enum class ErrorType {
    None,
    NoNetworkConnection,
    Other
}

fun Throwable.toErrorType(): ErrorType = when (this) {
    is NoNetworkConnectionException -> ErrorType.NoNetworkConnection
    else -> ErrorType.Other
}
