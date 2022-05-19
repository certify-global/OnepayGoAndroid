/*
 * Copyright (C) 2016 The Android Open Source Project
 * Modifications Copyright (c) 2017 CommonsWare, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.onepay.onepaygo.database.secureDB

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import net.sqlcipher.DatabaseErrorHandler
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteDatabaseHook
import net.sqlcipher.database.SQLiteException
import net.sqlcipher.database.SQLiteOpenHelper
import java.util.*

/**
 * SupportSQLiteOpenHelper implementation that works with SQLCipher for Android
 */
internal class Helper(
    context: Context, name: String?, callback: SupportSQLiteOpenHelper.Callback, passphrase: ByteArray?,
    options: SafeHelperFactory.Options
) : SupportSQLiteOpenHelper {
    private val delegate: OpenHelper
    private val passphrase: ByteArray?
    private val clearPassphrase: Boolean
    private fun createDelegate(
        context: Context, name: String?,
        callback: SupportSQLiteOpenHelper.Callback, options: SafeHelperFactory.Options
    ): OpenHelper {
        val dbRef = arrayOfNulls<SafeDatabase>(1)
        return OpenHelper(context, name, dbRef, callback, options)
    }

    /**
     * {@inheritDoc}
     */
    @Synchronized
    override fun getDatabaseName(): String? {
        return delegate.databaseName
    }

    /**
     * {@inheritDoc}
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Synchronized
    override fun setWriteAheadLoggingEnabled(enabled: Boolean) {
        delegate.setWriteAheadLoggingEnabled(enabled)
    }

    /**
     * {@inheritDoc}
     *
     * NOTE: by default, this implementation zeros out the passphrase after opening the
     * database
     */
    @Synchronized
    override fun getWritableDatabase(): SupportSQLiteDatabase {
        val result: SupportSQLiteDatabase
        try {
            result = delegate.getWritableSupportDatabase(passphrase)
        } catch (e: SQLiteException) {
            if (passphrase != null) {
                var isCleared = true
                for (b in passphrase) {
                    isCleared = isCleared && b == 0.toByte()
                }
                check(!isCleared) {
                    "The passphrase appears to be cleared. This happens by" +
                            "default the first time you use the factory to open a database, so we can remove the" +
                            "cleartext passphrase from memory. If you close the database yourself, please use a" +
                            "fresh SafeHelperFactory to reopen it. If something else (e.g., Room) closed the" +
                            "database, and you cannot control that, use SafeHelperFactory.Options to opt out of" +
                            "the automatic password clearing step. See the project README for more information."
                }
                if (clearPassphrase && passphrase != null) {
                    Arrays.fill(passphrase, 0.toByte())
                }
            }
            throw e
        }
        if (clearPassphrase && passphrase != null) {
            Arrays.fill(passphrase, 0.toByte())
        }
        return result
    }

    /**
     * {@inheritDoc}
     *
     * NOTE: this implementation delegates to getWritableDatabase(), to ensure
     * that we only need the passphrase once
     */
    override fun getReadableDatabase(): SupportSQLiteDatabase {
        return writableDatabase
    }

    /**
     * {@inheritDoc}
     */
    @Synchronized
    override fun close() {
        delegate.close()
    }

    internal class OpenHelper(
        context: Context?, name: String?, private val dbRef: Array<SafeDatabase?>, val callback: SupportSQLiteOpenHelper.Callback,
        options: SafeHelperFactory.Options?
    ) : SQLiteOpenHelper(context, name, null, callback.version, object : SQLiteDatabaseHook {
        override fun preKey(database: SQLiteDatabase) {
            if (options != null && options.preKeySql != null) {
                database.rawExecSQL(options.preKeySql)
            }
        }

        override fun postKey(database: SQLiteDatabase) {
            if (options != null && options.postKeySql != null) {
                database.rawExecSQL(options.postKeySql)
            }
        }
    }, DatabaseErrorHandler {
        val db = dbRef[0]
        if (db != null) {
            callback.onCorruption(db)
        }
    }) {
        @Volatile
        private var migrated = false
        @Synchronized
        fun getWritableSupportDatabase(passphrase: ByteArray?): SupportSQLiteDatabase {
            migrated = false
            val db = super.getWritableDatabase(passphrase)
            if (migrated) {
                close()
                return getWritableSupportDatabase(passphrase)
            }
            return getWrappedDb(db)!!
        }

        @Synchronized
        fun getWrappedDb(db: SQLiteDatabase): SafeDatabase? {
            var wrappedDb = dbRef[0]
            if (wrappedDb == null) {
                wrappedDb = SafeDatabase(db)
                dbRef[0] = wrappedDb
            }
            return dbRef[0]
        }

        /**
         * {@inheritDoc}
         */
        override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
            callback.onCreate(getWrappedDb(sqLiteDatabase)!!)
        }

        /**
         * {@inheritDoc}
         */
        override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            migrated = true
            callback.onUpgrade(getWrappedDb(sqLiteDatabase)!!, oldVersion, newVersion)
        }

        /**
         * {@inheritDoc}
         */
        override fun onConfigure(db: SQLiteDatabase) {
            callback.onConfigure(getWrappedDb(db)!!)
        }

        /**
         * {@inheritDoc}
         */
        override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            migrated = true
            callback.onDowngrade(getWrappedDb(db)!!, oldVersion, newVersion)
        }

        /**
         * {@inheritDoc}
         */
        override fun onOpen(db: SQLiteDatabase) {
            if (!migrated) {
                // from Google: "if we've migrated, we'll re-open the db so we  should not call the callback."
                callback.onOpen(getWrappedDb(db)!!)
            }
        }

        /**
         * {@inheritDoc}
         */
        @Synchronized
        override fun close() {
            super.close()
            dbRef[0] = null
        }
    }

    init {
        SQLiteDatabase.loadLibs(context)
        clearPassphrase = true
        delegate = createDelegate(context, name, callback, options)
        this.passphrase = passphrase
    }
}