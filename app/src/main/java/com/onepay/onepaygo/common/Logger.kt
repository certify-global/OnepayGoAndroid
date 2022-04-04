package com.onepay.onepaygo.common;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Logger {
    public static void debug(String classname, String message) {
       // if (EndPoints.deployment == EndPoints.Mode.Demo)
            Log.d(classname, "" + message);
    }

    public static void toast(Context context, String message) {
        Toast.makeText(context, message + "", Toast.LENGTH_SHORT).show();
    }

    public static void error(String classname, String message) {
        //if (EndPoints.deployment == EndPoints.Mode.Demo) {
            if (message == null) message = "null";
            Log.e(classname, message);
       // }

    }
}
