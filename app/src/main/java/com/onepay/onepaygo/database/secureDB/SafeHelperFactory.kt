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

import android.content.Context
import android.text.Editable
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import net.sqlcipher.database.SQLiteDatabase
import java.util.*

/**
 * SupportSQLiteOpenHelper.Factory implementation, for use with Room
 * and similar libraries, that supports SQLCipher for Android.
 */
class SafeHelperFactory
/**
 * Standard constructor.
 *
 * Note that the passphrase supplied here will be filled in with zeros after
 * the database is opened. Ideally, you should not create additional copies
 * of this passphrase, particularly as String objects.
 *
 * @param passphrase user-supplied passphrase to use for the database
 */ @JvmOverloads constructor(private val passphrase: ByteArray?, private val options: Options = Options.Builder().build()) : SupportSQLiteOpenHelper.Factory {
    /**
     * Standard constructor.
     *
     * Note that the passphrase supplied here will be filled in with zeros after
     * the database is opened. Ideally, you should not create additional copies
     * of this passphrase, particularly as String objects.
     *
     * If you are using an EditText to collect the passphrase from the user,
     * call getText() on the EditText, and pass that Editable to the
     * SafeHelperFactory.fromUser() factory method.
     *
     * @param passphrase user-supplied passphrase to use for the database
     * @param postKeySql optional callback to be called after database has been
     * "keyed" but before any database access is performed
     */
    /**
     * Standard constructor.
     *
     * Note that the passphrase supplied here will be filled in with zeros after
     * the database is opened. Ideally, you should not create additional copies
     * of this passphrase, particularly as String objects.
     *
     * If you are using an EditText to collect the passphrase from the user,
     * call getText() on the EditText, and pass that Editable to the
     * SafeHelperFactory.fromUser() factory method.
     *
     * @param passphrase user-supplied passphrase to use for the database
     */
    @JvmOverloads
    constructor(passphrase: CharArray, postKeySql: String? = null as String?) : this(SQLiteDatabase.getBytes(passphrase), postKeySql) {
        if (options.clearPassphrase) {
            clearPassphrase(passphrase)
        }
    }

    /**
     * Standard constructor.
     *
     * Note that the passphrase supplied here will be filled in with zeros after
     * the database is opened. Ideally, you should not create additional copies
     * of this passphrase, particularly as String objects.
     *
     * If you are using an EditText to collect the passphrase from the user,
     * call getText() on the EditText, and pass that Editable to the
     * SafeHelperFactory.fromUser() factory method.
     *
     * @param passphrase user-supplied passphrase to use for the database
     * @param options options for pre-key, post-key SQL
     */
    constructor(passphrase: CharArray, options: Options) : this(SQLiteDatabase.getBytes(passphrase), options) {
        if (options.clearPassphrase) {
            clearPassphrase(passphrase)
        }
    }

    /**
     * Standard constructor.
     *
     * Note that the passphrase supplied here will be filled in with zeros after
     * the database is opened. Ideally, you should not create additional copies
     * of this passphrase, particularly as String objects.
     *
     * @param passphrase user-supplied passphrase to use for the database
     * @param postKeySql optional callback to be called after database has been
     * "keyed" but before any database access is performed
     */
    constructor(passphrase: ByteArray?, postKeySql: String?) : this(passphrase, Options.Builder().setPostKeySql(postKeySql).build()) {}

    /**
     * {@inheritDoc}
     */
    override fun create(
        configuration: SupportSQLiteOpenHelper.Configuration
    ): SupportSQLiteOpenHelper {
        return create(
            configuration.context, configuration.name,
            configuration.callback
        )
    }

    fun create(
        context: Context, name: String?,
        callback: SupportSQLiteOpenHelper.Callback
    ): SupportSQLiteOpenHelper {
        return Helper(context, name, callback, passphrase, options)
    }

    private fun clearPassphrase(passphrase: CharArray) {
        Arrays.fill(passphrase, 0.toByte().toChar())
    }

    /**
     * Class for encapsulating pre- and post-key SQL statements to be executed as
     * part of opening the database. Use the static builder() method to get a Builder
     * for creating one of these.
     */
    class Options private constructor(
        /**
         * SQL to be executed before keying the database
         */
        val preKeySql: String?,
        /**
         * SQL to be executed after keying the database
         */
        val postKeySql: String?, /*
     * True if we should clear the in-memory cached copy of the passphrase after
     * opening the database; false otherwise. Defaults to true.
     */
        val clearPassphrase: Boolean
    ) {
        /**
         * A builder of Options objects. Use the builder() method on Options to create
         * one of these, call various setters to configure it, then call build() to
         * create the Options matching your requested specifications.
         */
        class Builder {
            private var preKeySql: String? = null
            private var postKeySql: String? = null
            private var clearPassphrase = true

            /**
             * @param preKeySql SQL to be executed before keying the database
             * @return the builder, for further configuration
             */
            fun setPreKeySql(preKeySql: String?): Builder {
                this.preKeySql = preKeySql
                return this
            }

            /**
             * @param postKeySql SQL to be executed after keying the database
             * @return the builder, for further configuration
             */
            fun setPostKeySql(postKeySql: String?): Builder {
                this.postKeySql = postKeySql
                return this
            }

            /**
             * @param value true if we should clear the in-memory cached copy of the passphrase after
             * opening the database; false otherwise. Defaults to true.
             * @return the builder, for further configuration
             */
            fun setClearPassphrase(value: Boolean): Builder {
                clearPassphrase = value
                return this
            }

            /**
             * @return the Options object containing your requested SQL
             */
            fun build(): Options {
                return Options(preKeySql, postKeySql, clearPassphrase)
            }
        }

        companion object {
            /**
             * @return a Builder to use to create an Options instance
             */
            fun builder(): Builder {
                return Builder()
            }
        }
    }

    companion object {
        const val POST_KEY_SQL_MIGRATE = "PRAGMA cipher_migrate;"
        const val POST_KEY_SQL_V3 = "PRAGMA cipher_compatibility = 3;"
        /**
         * Creates a SafeHelperFactory from an Editable, such as what you get by
         * calling getText() on an EditText.
         *
         * The Editable will be cleared as part of this call.
         *
         * @param editor the user's supplied passphrase
         * @param postKeySql optional SQL to be executed after database has been
         * "keyed" but before any other database access is performed
         * @return a SafeHelperFactory
         */
        /**
         * Creates a SafeHelperFactory from an Editable, such as what you get by
         * calling getText() on an EditText.
         *
         * The Editable will be cleared as part of this call.
         *
         * @param editor the user's supplied passphrase
         * @return a SafeHelperFactory
         */
        @JvmOverloads
        fun fromUser(editor: Editable, postKeySql: String? = null as String?): SafeHelperFactory {
            return fromUser(editor, Options.builder().setPostKeySql(postKeySql).build())
        }

        /**
         * Creates a SafeHelperFactory from an Editable, such as what you get by
         * calling getText() on an EditText.
         *
         * The Editable will be cleared as part of this call.
         *
         * @param editor the user's supplied passphrase
         * @param options options for pre-key, post-key SQL
         * @return a SafeHelperFactory
         */
        fun fromUser(editor: Editable, options: Options): SafeHelperFactory {
            val passphrase = CharArray(editor.length)
            val result: SafeHelperFactory
            editor.getChars(0, editor.length, passphrase, 0)
            result = try {
                SafeHelperFactory(passphrase, options)
            } finally {
                editor.clear()
                if (passphrase.size > 0) {
                    Arrays.fill(passphrase, 0.toByte().toChar())
                }
            }
            return result
        }

        /**
         * Changes the passphrase associated with this database. The
         * char[] is *not* cleared by this method -- please zero it
         * out if you are done with it.
         *
         * This will not encrypt an unencrypted database. Please use the
         * encrypt() method for that.
         *
         * @param db the database to rekey
         * @param passphrase the new passphrase to use
         */
        fun rekey(db: SupportSQLiteDatabase?, passphrase: CharArray?) {
            if (db is SafeDatabase) {
                db.rekey(passphrase)
            } else {
                throw IllegalArgumentException("Database is not from CWAC-SafeRoom")
            }
        }

        /**
         * Changes the passphrase associated with this database. The supplied
         * Editable is cleared as part of this operation.
         *
         * This will not encrypt an unencrypted database. Please use the
         * encrypt() method for that.
         *
         * @param db the database to rekey
         * @param editor source of passphrase, presumably from a user
         */
        fun rekey(db: SupportSQLiteDatabase?, editor: Editable) {
            if (db is SafeDatabase) {
                db.rekey(editor)
            } else {
                throw IllegalArgumentException("Database is not from CWAC-SafeRoom")
            }
        }
    }
    /**
     * Standard constructor.
     *
     * Note that the passphrase supplied here will be filled in with zeros after
     * the database is opened. Ideally, you should not create additional copies
     * of this passphrase, particularly as String objects.
     *
     * If you are using an EditText to collect the passphrase from the user,
     * call getText() on the EditText, and pass that Editable to the
     * SafeHelperFactory.fromUser() factory method.
     *
     * @param passphrase user-supplied passphrase to use for the database
     * @param options options for pre-key, post-key SQL
     */
}