package io.plastique.util

import io.reactivex.Single

@Suppress("unused")
sealed class Result<T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Error<T>(val error: Throwable) : Result<T>()
}

fun <T : Any> Single<T>.toResult(): Single<Result<T>> {
    return map<Result<T>> { Result.Success(it) }
        .onErrorReturn { error -> Result.Error(error) }
}
