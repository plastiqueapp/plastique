package io.plastique.util

import androidx.room.RoomDatabase
import androidx.room.RxRoom
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

object RxRoom {
    fun <T : Any> createObservable(database: RoomDatabase, tableNames: Array<String>, callable: () -> T): Observable<T> {
        return RxRoom.createObservable(database, *tableNames)
                .observeOn(Schedulers.from(database.queryExecutor))
                .flatMapMaybe { Maybe.fromCallable(callable) }
    }
}
