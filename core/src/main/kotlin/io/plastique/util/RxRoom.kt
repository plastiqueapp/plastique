package io.plastique.util

import androidx.room.RoomDatabase
import androidx.room.RxRoom
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

object RxRoom {
    fun <T : Any> createObservable(database: RoomDatabase, tableNames: Array<String>, callable: () -> T): Observable<T> {
        val scheduler = Schedulers.from(database.queryExecutor)
        val maybe = Maybe.fromCallable { database.runInTransaction(callable) }
        return RxRoom.createObservable(database, *tableNames)
                .subscribeOn(scheduler)
                .unsubscribeOn(scheduler)
                .observeOn(scheduler)
                .flatMapMaybe { maybe }
    }
}
