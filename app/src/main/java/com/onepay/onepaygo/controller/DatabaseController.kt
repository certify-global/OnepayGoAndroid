package com.onepay.onepaygo.controller

import android.content.Context
import com.onepay.onepaygo.Application
import com.onepay.onepaygo.database.Database
import com.onepay.onepaygo.database.DatabaseStore
import com.onepay.onepaygo.database.secureDB.SQLCipherUtils
import com.onepay.onepaygo.model.TransactionDB
import net.sqlcipher.database.SQLiteException
import java.io.File

class DatabaseController {
    private var listener: OnDbUpdate? = null

    interface OnDbUpdate {
        fun onDbUpdate()
    }

    fun init(context: Context?, passphrase: String?) {
        mContext = context
        databaseStore = Database.create(mContext!!, passphrase).databaseStore()
    }

    fun setListener(callbackListener: OnDbUpdate?) {
        listener = callbackListener
    }

    fun insertRecordToDB(vaccinationRecord: ArrayList<TransactionDB>?) {
        try {
            if (databaseStore != null) {
                databaseStore!!.insertAll(vaccinationRecord)
            }
        } catch (e: SQLiteException) {
            handleDBException(e)
        }
    }

    fun findAllRecords(): ArrayList<TransactionDB> {
        try {
            if (databaseStore != null) {
                return databaseStore!!.findAllRecord()
            }
        } catch (e: SQLiteException) {
            handleDBException(e)
        }
        return ArrayList<TransactionDB>()
    }

    companion object {
        private val TAG = DatabaseController::class.java.simpleName
        private var mInstance: DatabaseController? = null
        private var databaseStore: DatabaseStore? = null
        const val DB_VERSION = 1
        var mContext: Context? = null
        val instance: DatabaseController?
            get() {
                if (mInstance == null) {
                    mInstance = DatabaseController()
                }
                return mInstance
            }
    }

    private fun handleDBException(e: SQLiteException): Boolean {
        if (e.message!!.contains("file is not a database")) {
            val state: SQLCipherUtils.State = SQLCipherUtils.getDatabaseState(mContext!!.applicationContext, Database.DB_NAME)
            if (state === SQLCipherUtils.State.ENCRYPTED) {
                validateDB()
                init(mContext, Application.instance?.getAndroidID(mContext))
            } else if (state === SQLCipherUtils.State.DOES_NOT_EXIST) {
                init(mContext, Application.instance?.getAndroidID(mContext))
            }
            return true
        }
        return false
    }
    fun validateDB() {
        val databasesDir = File(mContext!!.applicationInfo.dataDir + "/databases")
        val file = File(databasesDir, Database.DB_NAME)
        if (file.exists()) {
            file.delete()
            val fileDbShm = File(databasesDir, "snap_face.db-shm")
            if (fileDbShm.exists()) {
                fileDbShm.delete()
            }
            val fileDbWal = File(databasesDir, "snap_face.db-wal")
            if (fileDbWal.exists()) {
                fileDbWal.delete()
            }
        }
    }
}