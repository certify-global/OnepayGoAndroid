package com.onepay.miura.common

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.Window
import com.onepay.miura.R

class Utils {
    companion object{
        private val TAG = Utils::class.java.name

        fun showDialog(context: Context?): Dialog? {
            val dialog = Dialog(context!!)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.prograss_dialog)
            if (dialog.window == null) return null
            dialog.setCancelable(false)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            return dialog
        }
        fun isConnectingToInternet(context: Context): Boolean {
            try {
                var result = false // Returns connection type. 0: none; 1: mobile data; 2: wifi; 3: vpn
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    cm?.run {
                        cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                            if (hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                return true
                            } else if (hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                                return true
                            } else if (hasTransport(NetworkCapabilities.TRANSPORT_VPN)){
                                return true
                            }
                        }
                    }
                } else {
                    cm?.run {
                        cm.activeNetworkInfo?.run {
                            if (type == ConnectivityManager.TYPE_WIFI) {
                                return true
                            } else if (type == ConnectivityManager.TYPE_MOBILE) {
                                return true
                            } else if(type == ConnectivityManager.TYPE_VPN) {
                                return true
                            }
                        }
                    }
                }
                return result
            } catch (e: Exception) {
             //   Logger.error(com.onepay.common.Util.LOG + "isConnectingToInternet()", e.message)
            }
            return false
        }
    }
}