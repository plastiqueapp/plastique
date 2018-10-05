package io.plastique.inject.modules

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.plastique.AppDatabase
import io.plastique.core.analytics.Analytics
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import java.io.File
import javax.inject.Singleton

@Module
abstract class DatabaseModule {
    @Binds
    abstract fun bindRoomDatabase(database: AppDatabase): RoomDatabase

    @Module
    companion object {
        @Provides
        @Singleton
        @JvmStatic
        fun provideDatabase(context: Context, analytics: Analytics): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "main.db")
                    .fallbackToDestructiveMigration()
                    .openHelperFactory(RequerySQLiteOpenHelperFactory())
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            analytics.initDatabaseSize(File(db.path))
                        }
                    })
                    .build()
        }
    }
}
