package com.onepay.onepaygo.database

import android.content.Context
import com.onepay.onepaygo.common.Logger.debug
import com.onepay.onepaygo.model.TransactionDB
import com.onepay.onepaygo.database.DateTypeConverter
import com.onepay.onepaygo.database.DatabaseStore
import kotlin.jvm.Volatile
import android.text.SpannableStringBuilder
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.onepay.onepaygo.controller.DatabaseController
import com.onepay.onepaygo.database.secureDB.SafeHelperFactory
import java.lang.Exception

@androidx.room.Database(entities = [TransactionDB::class], version = DatabaseController.DB_VERSION, exportSchema = false)
@TypeConverters(DateTypeConverter::class)
abstract class Database : RoomDatabase() {
    abstract fun databaseStore(): DatabaseStore?

    companion object {
        const val DB_NAME = "one_pay_go.db"

        val INSTANCE: Database? = null
        fun create(ctxt: Context, passphrase: String?): Database {
            val b: Builder<Database>
            b = Room.databaseBuilder(ctxt.applicationContext, Database::class.java, DB_NAME).allowMainThreadQueries()
            b.openHelperFactory(SafeHelperFactory.fromUser(SpannableStringBuilder(passphrase)))
            b.allowMainThreadQueries()
            b.fallbackToDestructiveMigration()
            b.addCallback(object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                }
            })
            return b.build()
        }
    }
}