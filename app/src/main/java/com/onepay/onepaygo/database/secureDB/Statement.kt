/***
 * Copyright (c) 2017-2018 CommonsWare, LLC
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain	a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 * by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS,	WITHOUT	WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.onepay.onepaygo.database.secureDB

import androidx.sqlite.db.SupportSQLiteStatement
import net.sqlcipher.database.SQLiteStatement

/**
 * SupportSQLiteStatement implementation that wraps SQLCipher for Android's
 * SQLiteStatement
 */
internal class Statement(private val safeStatement: SQLiteStatement) : Program(safeStatement), SupportSQLiteStatement {
    /**
     * {@inheritDoc}
     */
    override fun execute() {
        safeStatement.execute()
    }

    /**
     * {@inheritDoc}
     */
    override fun executeUpdateDelete(): Int {
        return safeStatement.executeUpdateDelete()
    }

    /**
     * {@inheritDoc}
     */
    override fun executeInsert(): Long {
        return safeStatement.executeInsert()
    }

    /**
     * {@inheritDoc}
     */
    override fun simpleQueryForLong(): Long {
        return safeStatement.simpleQueryForLong()
    }

    /**
     * {@inheritDoc}
     */
    override fun simpleQueryForString(): String {
        return safeStatement.simpleQueryForString()
    }
}