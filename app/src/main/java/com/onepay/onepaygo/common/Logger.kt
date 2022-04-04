package com.onepay.onepaygo.common

import android.content.Context
import android.util.Log
import android.widget.Toast

object Logger {
    @JvmStatic
    fun debug(classname: String?, message: String) {
        // if (EndPoints.deployment == EndPoints.Mode.Demo)
        Log.d(classname, "" + message)
    }

    fun toast(context: Context?, message: String) {
        Toast.makeText(context, message + "", Toast.LENGTH_SHORT).show()
    }

    @JvmStatic
    fun error(classname: String?, message: String?) {
        //if (EndPoints.deployment == EndPoints.Mode.Demo) {
        var message = message
        if (message == null) message = "null"
        Log.e(classname, message)
        // }
    }
}