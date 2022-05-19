/***
 * Copyright (c) 2017 CommonsWare, LLC
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain	a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 * by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS,	WITHOUT	WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * Covered in detail in the book _The Busy Coder's Guide to Android Development_
 * https://commonsware.com/Android
 */
package com.onepay.onepaygo.database.secureDB

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteTransactionListener
import android.os.CancellationSignal
import android.text.Editable
import android.text.TextUtils
import android.util.Pair
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQuery
import androidx.sqlite.db.SupportSQLiteStatement
import net.sqlcipher.database.SQLiteCursor
import net.sqlcipher.database.SQLiteDatabase
import java.util.*

/**
 * A SupportSQLiteDatabase implementation that delegates to a SQLCipher
 * for Android implementation of SQLiteDatabase
 */
internal class SafeDatabase(private val safeDb: SQLiteDatabase) : SupportSQLiteDatabase {
    /**
     * {@inheritDoc}
     */
    override fun compileStatement(sql: String): SupportSQLiteStatement {
        return Statement(safeDb.compileStatement(sql))
    }

    /**
     * {@inheritDoc}
     */
    override fun beginTransaction() {
        safeDb.beginTransaction()
    }

    /**
     * {@inheritDoc}
     */
    override fun beginTransactionNonExclusive() {
        safeDb.beginTransactionNonExclusive()
    }

    /**
     * {@inheritDoc}
     */
    override fun beginTransactionWithListener(listener: SQLiteTransactionListener) {
        safeDb.beginTransactionWithListener(
            object : net.sqlcipher.database.SQLiteTransactionListener {
                override fun onBegin() {
                    listener.onBegin()
                }

                override fun onCommit() {
                    listener.onCommit()
                }

                override fun onRollback() {
                    listener.onRollback()
                }
            })
    }

    /**
     * {@inheritDoc}
     */
    override fun beginTransactionWithListenerNonExclusive(listener: SQLiteTransactionListener) {
        safeDb.beginTransactionWithListenerNonExclusive(
            object : net.sqlcipher.database.SQLiteTransactionListener {
                override fun onBegin() {
                    listener.onBegin()
                }

                override fun onCommit() {
                    listener.onCommit()
                }

                override fun onRollback() {
                    listener.onRollback()
                }
            })
    }

    /**
     * {@inheritDoc}
     */
    override fun endTransaction() {
        safeDb.endTransaction()
    }

    /**
     * {@inheritDoc}
     */
    override fun setTransactionSuccessful() {
        safeDb.setTransactionSuccessful()
    }

    /**
     * {@inheritDoc}
     */
    override fun inTransaction(): Boolean {
        if (safeDb.isOpen) {
            return safeDb.inTransaction()
        }
        throw IllegalStateException("You should not be doing this on a closed database")
    }

    /**
     * {@inheritDoc}
     */
    override fun isDbLockedByCurrentThread(): Boolean {
        if (safeDb.isOpen) {
            return safeDb.isDbLockedByCurrentThread
        }
        throw IllegalStateException("You should not be doing this on a closed database")
    }

    /**
     * {@inheritDoc}
     */
    override fun yieldIfContendedSafely(): Boolean {
        if (safeDb.isOpen) {
            return safeDb.yieldIfContendedSafely()
        }
        throw IllegalStateException("You should not be doing this on a closed database")
    }

    /**
     * {@inheritDoc}
     */
    override fun yieldIfContendedSafely(sleepAfterYieldDelay: Long): Boolean {
        if (safeDb.isOpen) {
            return safeDb.yieldIfContendedSafely(sleepAfterYieldDelay)
        }
        throw IllegalStateException("You should not be doing this on a closed database")
    }

    /**
     * {@inheritDoc}
     */
    override fun getVersion(): Int {
        return safeDb.version
    }

    /**
     * {@inheritDoc}
     */
    override fun setVersion(version: Int) {
        safeDb.version = version
    }

    /**
     * {@inheritDoc}
     */
    override fun getMaximumSize(): Long {
        return safeDb.maximumSize
    }

    /**
     * {@inheritDoc}
     */
    override fun setMaximumSize(numBytes: Long): Long {
        return safeDb.setMaximumSize(numBytes)
    }

    /**
     * {@inheritDoc}
     */
    override fun getPageSize(): Long {
        return safeDb.pageSize
    }

    /**
     * {@inheritDoc}
     */
    override fun setPageSize(numBytes: Long) {
        safeDb.pageSize = numBytes
    }

    /**
     * {@inheritDoc}
     */
    override fun query(sql: String): Cursor {
        return query(SimpleSQLiteQuery(sql))
    }

    /**
     * {@inheritDoc}
     */
    override fun query(sql: String, selectionArgs: Array<Any>): Cursor {
        return query(SimpleSQLiteQuery(sql, selectionArgs))
    }

    /**
     * {@inheritDoc}
     */
    override fun query(supportQuery: SupportSQLiteQuery): Cursor {
        return query(supportQuery)
    }

    /**
     * {@inheritDoc}
     */
    override fun query(
        supportQuery: SupportSQLiteQuery,
        signal: CancellationSignal
    ): Cursor {
        val hack = BindingsRecorder()
        supportQuery.bindTo(hack)
        return safeDb.rawQueryWithFactory(
            { db, masterQuery, editTable, query ->
                supportQuery.bindTo(Program(query))
                SQLiteCursor(db, masterQuery, editTable, query)
            }, supportQuery.sql, hack.getBindings(), null
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun insert(
        table: String, conflictAlgorithm: Int,
        values: ContentValues
    ): Long {
        return safeDb.insertWithOnConflict(table, null, values, conflictAlgorithm)
    }

    /**
     * {@inheritDoc}
     */
    override fun delete(table: String, whereClause: String, whereArgs: Array<Any>): Int {
        val query = ("DELETE FROM " + table
                + if (TextUtils.isEmpty(whereClause)) "" else " WHERE $whereClause")
        val statement = compileStatement(query)
        return try {
            SimpleSQLiteQuery.bind(statement, whereArgs)
            statement.executeUpdateDelete()
        } finally {
            try {
                statement.close()
            } catch (e: Exception) {
                throw RuntimeException("Exception attempting to close statement", e)
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun update(
        table: String, conflictAlgorithm: Int, values: ContentValues,
        whereClause: String, whereArgs: Array<Any>
    ): Int {
        // taken from SQLiteDatabase class.
        require(!(values == null || values.size() == 0)) { "Empty values" }
        val sql = StringBuilder(120)
        sql.append("UPDATE ")
        sql.append(CONFLICT_VALUES[conflictAlgorithm])
        sql.append(table)
        sql.append(" SET ")

        // move all bind args to one array
        val setValuesSize = values.size()
        val bindArgsSize = if (whereArgs == null) setValuesSize else setValuesSize + whereArgs.size
        val bindArgs = arrayOfNulls<Any>(bindArgsSize)
        var i = 0
        for (colName in values.keySet()) {
            sql.append(if (i > 0) "," else "")
            sql.append(colName)
            bindArgs[i++] = values[colName]
            sql.append("=?")
        }
        if (whereArgs != null) {
            i = setValuesSize
            while (i < bindArgsSize) {
                bindArgs[i] = whereArgs[i - setValuesSize]
                i++
            }
        }
        if (!TextUtils.isEmpty(whereClause)) {
            sql.append(" WHERE ")
            sql.append(whereClause)
        }
        val statement = compileStatement(sql.toString())
        return try {
            SimpleSQLiteQuery.bind(statement, bindArgs)
            statement.executeUpdateDelete()
        } finally {
            try {
                statement.close()
            } catch (e: Exception) {
                throw RuntimeException("Exception attempting to close statement", e)
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Throws(SQLException::class)
    override fun execSQL(sql: String) {
        safeDb.execSQL(sql)
    }

    /**
     * {@inheritDoc}
     */
    @Throws(SQLException::class)
    override fun execSQL(sql: String, bindArgs: Array<Any>) {
        safeDb.execSQL(sql, bindArgs)
    }

    /**
     * {@inheritDoc}
     */
    override fun isReadOnly(): Boolean {
        return safeDb.isReadOnly
    }

    /**
     * {@inheritDoc}
     */
    override fun isOpen(): Boolean {
        return safeDb.isOpen
    }

    /**
     * {@inheritDoc}
     */
    override fun needUpgrade(newVersion: Int): Boolean {
        return safeDb.needUpgrade(newVersion)
    }

    /**
     * {@inheritDoc}
     */
    override fun getPath(): String {
        return safeDb.path
    }

    /**
     * {@inheritDoc}
     */
    override fun setLocale(locale: Locale) {
        safeDb.setLocale(locale)
    }

    /**
     * {@inheritDoc}
     */
    override fun setMaxSqlCacheSize(cacheSize: Int) {
        safeDb.maxSqlCacheSize = cacheSize
    }

    /**
     * {@inheritDoc}
     */
    override fun setForeignKeyConstraintsEnabled(enable: Boolean) {
        safeDb.setForeignKeyConstraintsEnabled(enable)
    }

    /**
     * {@inheritDoc}
     */
    override fun enableWriteAheadLogging(): Boolean {
        return safeDb.enableWriteAheadLogging()
    }

    /**
     * {@inheritDoc}
     */
    override fun disableWriteAheadLogging() {
        safeDb.disableWriteAheadLogging()
    }

    /**
     * {@inheritDoc}
     */
    override fun isWriteAheadLoggingEnabled(): Boolean {
        return safeDb.isWriteAheadLoggingEnabled
    }

    /**
     * {@inheritDoc}
     */
    override fun getAttachedDbs(): List<Pair<String, String>> {
        return safeDb.attachedDbs
    }

    /**
     * {@inheritDoc}
     */
    override fun isDatabaseIntegrityOk(): Boolean {
        return safeDb.isDatabaseIntegrityOk
    }

    /**
     * {@inheritDoc}
     */
    override fun close() {
        safeDb.close()
    }

    /**
     * Changes the passphrase associated with this database. The
     * char[] is *not* cleared by this method -- please zero it
     * out if you are done with it.
     *
     * @param passphrase the new passphrase to use
     */
    fun rekey(passphrase: CharArray?) {
        safeDb.changePassword(passphrase)
    }

    /**
     * Changes the passphrase associated with this database. The supplied
     * Editable is cleared as part of this operation.
     *
     * @param editor source of passphrase, presumably from a user
     */
    fun rekey(editor: Editable) {
        val passphrase = CharArray(editor.length)
        editor.getChars(0, editor.length, passphrase, 0)
        try {
            rekey(passphrase)
        } finally {
            editor.clear()
            if (passphrase.size > 0) {
                Arrays.fill(passphrase, 0.toByte().toChar())
            }
        }
    }

    companion object {
        private val CONFLICT_VALUES = arrayOf("", " OR ROLLBACK ", " OR ABORT ", " OR FAIL ", " OR IGNORE ", " OR REPLACE ")
    }
}