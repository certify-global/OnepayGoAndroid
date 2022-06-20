package com.onepay.onepaygo

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import com.onepay.onepaygo.common.Utils
import com.onepay.onepaygo.controller.DatabaseController
import com.onepay.onepaygo.data.AppSharedPreferences
import java.lang.Exception
import java.security.MessageDigest
import java.util.*

class Application : android.app.Application() {
    var sharedPreferences: SharedPreferences? = null
    override fun onCreate() {
        super.onCreate()
        mInstance = this
        sharedPreferences = AppSharedPreferences.getSharedPreferences(this)
        if (sharedPreferences != null) {
            val value = true
            var pragmaKey = ""
            if (value) {
                pragmaKey = getAndroidID(this)
            }
            DatabaseController.instance?.init(this,pragmaKey)
        }
        if (BuildConfig.BUILD_TYPE == "release") {
            initAppCenter()
        }
    }

    fun getAndroidID(context: Context?): String {
        val macAddress: String = Utils.getAndroidID(context!!)!!
        return getSha256Hash(macAddress)
    }

    private fun getSha256Hash(password: String): String {
        val data = password.toByteArray()
        var hash: ByteArray? = null
        try {
            val md = MessageDigest.getInstance("SHA-256")
            val salt = byteArrayOf(
                0x1, 0x10, 0x33, 0x40, 0x5, 0x1, 0x7, 0x23, 0x1, 0x10, 0x11, 0x12, 0x13, 0x14,
                0x10, 0x70, 0x17, 0x18, 0x19, 0x1, 0x10, 0x33, 0x40, 0x5, 0x1, 0x7, 0x27,
                0x22, 0x33, 0x30, 0x10, 0x32
            )
            md.update(salt)
            md.update(data)
            hash = md.digest()
        } catch (e: Exception) {
            Log.e(TAG, "Error in creating secured data " + e.message)
        } finally {
            Arrays.fill(data, 0.toByte())
        }
        return Base64.encodeToString(hash, Base64.DEFAULT)
    }

    private fun initAppCenter() {
       // setAppCenterCrashListener() //Listener should be set before calling AppCenter start
//        AppCenter.setLogLevel(Log.VERBOSE)
//        AppCenter.start(
//            this, "0dcaccfc-1577-425b-96e8-8f2c1e48144d",
//            Analytics::class.java, Crashes::class.java
//        )
//        AppCenter.setUserId(getAndroidID(this))
//        Analytics.setEnabled(true)
//        Crashes.setEnabled(true)
//        CrashHandler.getInstance().init(this)
    }

//    private fun setAppCenterCrashListener() {
//        val crashesListener: AbstractCrashesListener = object : AbstractCrashesListener() {
//            fun shouldProcess(report: ErrorReport?): Boolean {
//                Log.i(TAG, "Should process")
//                return true
//            }
//
//            fun getErrorAttachments(report: ErrorReport?): Iterable<ErrorAttachmentLog> {
//                Log.d(TAG, "Initiate crash report sending")
//                val sp: SharedPreferences = Util.getSharedPreferences(applicationContext)
//                val binaryData: ByteArray = Util.getBytesFromFile(sp.getString(GlobalParameters.LogFilePath, ""))
//                val binaryLog: ErrorAttachmentLog = ErrorAttachmentLog.attachmentWithBinary(binaryData, "Crashlog.log", "text/plain")
//                return listOf<ErrorAttachmentLog>(binaryLog)
//            }
//
//            fun onSendingFailed(report: ErrorReport?, e: Exception?) {
//                Log.e(TAG, "Crash report sending failed")
//                LoggerUtil.deleteLogFile()
//            }
//
//            fun onSendingSucceeded(report: ErrorReport?) {
//                Log.d(TAG, "Success: Crash report sent")
//                LoggerUtil.deleteLogFile()
//            }
//        }
//        Crashes.setListener(crashesListener)
//    }

    companion object {
        private val TAG = android.app.Application::class.java.simpleName
        private var mInstance: Application? = null
        val instance: Application?
            get() = mInstance
    }
}