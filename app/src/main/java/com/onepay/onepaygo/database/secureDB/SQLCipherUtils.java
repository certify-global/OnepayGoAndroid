/*
 * Copyright (c) 2012-2017 CommonsWare, LLC
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

package com.onepay.onepaygo.database.secureDB;

import android.content.Context;
import android.text.Editable;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteStatement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

public class SQLCipherUtils {
  /**
   * The detected state of the database, based on whether we can open it
   * without a passphrase.
   */
  public enum State {
    DOES_NOT_EXIST, UNENCRYPTED, ENCRYPTED
  }

  /**
   * Determine whether or not this database appears to be encrypted, based
   * on whether we can open it without a passphrase.
   *
   * @param ctxt a Context
   * @param dbName the name of the database, as used with Room, SQLiteOpenHelper,
   *               etc.
   * @return the detected state of the database
   */
  public static State getDatabaseState(Context ctxt, String dbName) {
    SQLiteDatabase.loadLibs(ctxt);

    return(getDatabaseState(ctxt.getDatabasePath(dbName)));
  }

  /**
   * Determine whether or not this database appears to be encrypted, based
   * on whether we can open it without a passphrase.
   *
   * NOTE: You are responsible for ensuring that net.sqlcipher.database.SQLiteDatabase.loadLibs()
   * is called before calling this method. This is handled automatically with the
   * getDatabaseState() method that takes a Context as a parameter.
   *
   * @param dbPath a File pointing to the database
   * @return the detected state of the database
   */
  public static State getDatabaseState(File dbPath) {
    if (dbPath.exists()) {
      SQLiteDatabase db=null;

      try {
        db=
          SQLiteDatabase.openDatabase(dbPath.getAbsolutePath(), "",
            null, SQLiteDatabase.OPEN_READONLY);

        db.getVersion();

        return(State.UNENCRYPTED);
      }
      catch (Exception e) {
        return(State.ENCRYPTED);
      }
      finally {
        if (db != null) {
          db.close();
        }
      }
    }

    return(State.DOES_NOT_EXIST);
  }
}
