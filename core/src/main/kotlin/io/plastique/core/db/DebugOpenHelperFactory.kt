package io.plastique.core.db

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.jakewharton.processphoenix.ProcessPhoenix

class DebugOpenHelperFactory(
    private val openHelperFactory: SupportSQLiteOpenHelper.Factory = FrameworkSQLiteOpenHelperFactory()
) : SupportSQLiteOpenHelper.Factory {

    override fun create(configuration: SupportSQLiteOpenHelper.Configuration): SupportSQLiteOpenHelper {
        val config = if (configuration.name != null) {
            SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
                    .name(configuration.name)
                    .callback(OpenHelper(configuration.context, configuration.name!!, configuration.callback))
                    .build()
        } else {
            configuration
        }
        return openHelperFactory.create(config)
    }

    private class OpenHelper(
        private val context: Context,
        private val databaseName: String,
        private val delegate: SupportSQLiteOpenHelper.Callback
    ) : SupportSQLiteOpenHelper.Callback(delegate.version) {
        override fun onConfigure(db: SupportSQLiteDatabase) {
            delegate.onConfigure(db)
        }

        override fun onCreate(db: SupportSQLiteDatabase) {
            delegate.onCreate(db)
        }

        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
            delegate.onUpgrade(db, oldVersion, newVersion)
        }

        override fun onDowngrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
            delegate.onDowngrade(db, oldVersion, newVersion)
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            try {
                delegate.onOpen(db)
            } catch (e: IllegalStateException) {
                db.close()
                context.deleteDatabase(databaseName)
                ProcessPhoenix.triggerRebirth(context)
                throw e
            }
        }

        override fun onCorruption(db: SupportSQLiteDatabase) {
            delegate.onCorruption(db)
        }
    }
}
