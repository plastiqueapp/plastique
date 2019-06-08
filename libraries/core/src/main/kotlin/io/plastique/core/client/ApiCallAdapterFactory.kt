package io.plastique.core.client

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.MaybeEmitter
import io.reactivex.MaybeOnSubscribe
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import javax.inject.Inject

class ApiCallAdapterFactory @Inject constructor(
    private val callExecutor: CallExecutor
) : CallAdapter.Factory() {

    override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        val rawType = getRawType(returnType)
        if (rawType == Completable::class.java) {
            return RxJava2CallAdapter<Any, Completable>(Void::class.java, callExecutor) { it.ignoreElement() }
        }
        if (rawType != Single::class.java && rawType != Maybe::class.java) {
            return null
        }
        require(returnType is ParameterizedType) { "${rawType.simpleName} must be parametrized" }
        val responseType = getParameterUpperBound(0, returnType)
        return when (rawType) {
            Single::class.java -> RxJava2CallAdapter<Any, Single<Any>>(responseType, callExecutor) { it.toSingle() }
            else -> RxJava2CallAdapter<Any, Maybe<Any>>(responseType, callExecutor) { it }
        }
    }

    private class RxJava2CallAdapter<T, R>(
        private val responseType: Type,
        private val callExecutor: CallExecutor,
        private val transformer: (Maybe<T>) -> R
    ) : CallAdapter<T, R> {
        override fun responseType(): Type = responseType

        override fun adapt(call: Call<T>): R {
            return transformer(Maybe.create(CallOnSubscribe(call, callExecutor)))
        }
    }

    private class CallOnSubscribe<T>(
        private val call: Call<T>,
        private val callExecutor: CallExecutor
    ) : MaybeOnSubscribe<T> {
        override fun subscribe(emitter: MaybeEmitter<T>) {
            val callForObserver = call.clone()
            val disposable = CallDisposable(callForObserver)
            emitter.setDisposable(disposable)

            val response = callExecutor.execute(callForObserver)
            val body = response.body()
            if (body != null) {
                emitter.onSuccess(body)
            } else {
                emitter.onComplete()
            }
        }
    }

    private class CallDisposable(private val call: Call<*>) : Disposable {
        @Volatile private var disposed: Boolean = false

        override fun dispose() {
            disposed = true
            call.cancel()
        }

        override fun isDisposed(): Boolean = disposed
    }
}
