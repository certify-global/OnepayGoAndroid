package com.onepay.miura.common

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.onepay.miura.R

class Utils {
    companion object {
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
                var result =
                    false // Returns connection type. 0: none; 1: mobile data; 2: wifi; 3: vpn
                val cm =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    cm?.run {
                        cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                            if (hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                return true
                            } else if (hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                                return true
                            } else if (hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
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
                            } else if (type == ConnectivityManager.TYPE_VPN) {
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

        fun hideKeyboard(activity: Activity) {
            try {
                val view = activity.currentFocus
                val methodManager =
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                methodManager.hideSoftInputFromWindow(
                    view!!.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            } catch (e: Exception) {
                Logger.error(TAG, e.message)
            }
        }

        fun showKeyboard(activity: Activity) {
            try {
                val view = activity.currentFocus
                val methodManager =
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                methodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            } catch (e: Exception) {
                Logger.error(TAG, e.message)
            }
        }

        fun ValidationMMYY(str: String): Boolean {
            val arrayMMYY = str.split("/")
            if (arrayMMYY.size < 2) return false
            if (arrayMMYY.size == 1 || arrayMMYY.size == 2) {
                if ((arrayMMYY[0].isNotEmpty() && arrayMMYY[0].toInt() > 13)) {
                    return false
                }
            }
            if (arrayMMYY.size == 2)
                if ((arrayMMYY[1].isNotEmpty() && arrayMMYY[1].toInt() < 21)) {
                    return false
                }
            return true
        }
        fun openDialogDevice(context: Context) {
            val d = Dialog(context)
            d.requestWindowFeature(Window.FEATURE_NO_TITLE)
            d.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            d.setCancelable(false)
            d.setContentView(R.layout.dialog_device)
            val tv_header = d.findViewById<TextView>(R.id.tv_header_settings)
            val tv_setting = d.findViewById<TextView>(R.id.tv_setting)
            val tv_cancel = d.findViewById<TextView>(R.id.tv_cancel)
            tv_setting.setOnClickListener {
                d.dismiss()
               //nav Settings
            }
            tv_cancel.setOnClickListener { d.dismiss() }
            d.show()
        }

    }
}