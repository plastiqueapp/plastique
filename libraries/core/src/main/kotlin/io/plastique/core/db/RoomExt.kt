package io.plastique.core.db

import androidx.room.RoomDatabase
import androidx.room.RxRoom
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

fun <T : Any> RoomDatabase.createObservable(vararg tableNames: String, callable: () -> T): Observable<T> {
    val scheduler = Schedulers.from(transactionExecutor)
    val maybe = Maybe.fromCallable { runInTransaction(callable) }
    return RxRoom.createObservable(this, *tableNames)
        .subscribeOn(scheduler)
        .unsubscribeOn(scheduler)
        .observeOn(scheduler)
        .flatMapMaybe { maybe }
}
