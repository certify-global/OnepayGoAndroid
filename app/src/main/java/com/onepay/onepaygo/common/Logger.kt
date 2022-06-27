package com.onepay.onepaygo.common

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.microsoft.appcenter.analytics.Analytics

object Logger {
    private val properties: HashMap<String?, String?> = object : HashMap<String?, String?>() {
        init {
            put("manufacturer ", Build.MANUFACTURER)
            put("model ", Build.MODEL)
        }
    }
    @JvmStatic
    fun debug(classname: String?, message: String) {
         if (Constants.deployment == Constants.ModeApp.Demo)
        Log.d(classname, "" + message)
    }

    fun toast(context: Context?, message: String) {
        Toast.makeText(context, message + "", Toast.LENGTH_SHORT).show()
    }

    @JvmStatic
    fun error(classname: String?, message: String?) {
        if (Constants.deployment == Constants.ModeApp.Demo){
        var message = message
        if (message == null) message = "null"
        Log.e(classname, message)
         }
    }
    fun error(classname: String, methodName: String, message: String) {
        Log.e(classname, "$methodName-$message")
        Analytics.trackEvent("$classname $methodName $message", properties)
    }

    fun verbose(tag: String?, format: String?, params: Any?) {
        Log.v(tag, String.format(format!!, params))
    }

    fun warn(tag: String?, format: String?, params: Any?) {
        val message = String.format(format!!, params)
        Log.w(tag, message)
        Analytics.trackEvent(message, properties)
    }

    fun info(tag: String?, format: String?, params: Any?) {
        val message = String.format(format!!, params)
        Log.w(tag, message)
        Analytics.trackEvent(message, properties)
    }
}