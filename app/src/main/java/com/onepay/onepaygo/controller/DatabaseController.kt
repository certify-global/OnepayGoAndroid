package com.onepay.onepaygo.controller

import android.content.Context
import com.onepay.onepaygo.Application
import com.onepay.onepaygo.common.Constants
import com.onepay.onepaygo.database.Database
import com.onepay.onepaygo.database.DatabaseStore
import com.onepay.onepaygo.database.secureDB.SQLCipherUtils
import com.onepay.onepaygo.model.ReportRecords
import net.sqlcipher.database.SQLiteException
import java.io.File

class DatabaseController {

    fun init(context: Context?, passphrase: String?) {
        mContext = context
        databaseStore = Database.create(mContext!!, passphrase).databaseStore()
    }

       fun insertRecordToDB(reportRecordsList: List<ReportRecords>?) {
        try {
            if (databaseStore != null) {
                databaseStore!!.insertAll(reportRecordsList)
            }
        } catch (e: SQLiteException) {
            handleDBException(e)
        }
    }

    fun DbRecordsSearch(searchType: Int, dateVal: String,limit:Int,offsetValue:Int,value: String,mTerminalId :Int): List<ReportRecords> {
        try {
            if (databaseStore != null) {
                when (searchType) {
                    Constants.SearchType.All.value -> {
                        return databaseStore!!.findAllRecord(dateVal,limit,offsetValue)
                    }
                    Constants.SearchType.TransactionID.value -> {

                        return databaseStore!!.transactionIdSearch(dateVal,limit,offsetValue,value,mTerminalId)
                    }
                    Constants.SearchType.FirstName.value -> {

                        return  databaseStore!!.firstNameSearch(dateVal,limit,offsetValue,value,mTerminalId)
                    }
                    Constants.SearchType.LastName.value -> {

                        return databaseStore!!.lastNameSearch(dateVal,limit,offsetValue,value,mTerminalId)
                    }
                    Constants.SearchType.CustomerID.value -> {

                        return databaseStore!!.customerIdSearch(dateVal,limit,offsetValue,value,mTerminalId)
                    }
                    Constants.SearchType.Email.value -> {

                        return databaseStore!!.emailSearch(dateVal,limit,offsetValue,value,mTerminalId)
                    }
                    Constants.SearchType.Phone.value -> {

                        return databaseStore!!.phoneNumberSearch(dateVal,limit,offsetValue,value,mTerminalId)
                    }
                    Constants.SearchType.TransactionAmount.value -> {

                        return databaseStore!!.transactionAmountSearch(dateVal,limit,offsetValue,value,mTerminalId)
                    }
                    Constants.SearchType.CardLast4Digits.value -> {

                        return databaseStore!!.last4Search(dateVal,limit,offsetValue,value,mTerminalId)
                    }
                    Constants.SearchType.SourceApplication.value -> {

                        return databaseStore!!.sourceApplicationSearch(dateVal,limit,offsetValue,value,mTerminalId)
                    }
                }
            }
        } catch (e: SQLiteException) {
            handleDBException(e)
        }
        return ArrayList();
    }




    companion object {
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