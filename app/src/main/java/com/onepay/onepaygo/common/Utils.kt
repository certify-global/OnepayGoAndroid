package com.onepay.onepaygo.common

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.onepay.onepaygo.R
import com.onepay.onepaygo.callback.CallbackInterface
import com.onepay.onepaygo.data.AppSharedPreferences

class Utils {
    companion object {
        private val TAG = Build::class.java.name
        const val PERMISSION_REQUEST_CODE = 200
        private const val REQUEST_CHECK_SETTINGS = 200
        val location = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        fun PermissionCheck(context: Context?) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(
                        context!!,
                        Manifest.permission.BLUETOOTH
                    ) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_ADMIN
                    ) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.INTERNET
                    ) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_NETWORK_STATE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    val permissionList = arrayOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE
                    )
                    ActivityCompat.requestPermissions(
                        (context as Activity?)!!,
                        permissionList,
                        PERMISSION_REQUEST_CODE
                    )
                }
            }
        }

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

        fun openDialogDevice(context: Context, activity: Activity) {
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
                activity.findNavController(R.id.nav_left_menu_container)
                    .navigate(R.id.settingFragment)
            }
            tv_cancel.setOnClickListener { d.dismiss() }
            d.show()
        }
        fun openDialogVoid(
            context: Context,
            msg: String,
            header: String?,
            callbackInterface: CallbackInterface?
        ) {
            try {
                val d = Dialog(context)
                d.requestWindowFeature(Window.FEATURE_NO_TITLE)
                d.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                d.setCancelable(false)
                d.setContentView(R.layout.dialog_header_message)
                val tv_approved = d.findViewById<TextView>(R.id.tv_approved)
                val tv_header = d.findViewById<TextView>(R.id.tv_header)
                val btn_continue = d.findViewById<TextView>(R.id.btn_continue)
                tv_header.text = header
                tv_approved.text = "     $msg   "
                btn_continue.setOnClickListener {
                    if (msg == "Connected to BBPOS") {
                        d.dismiss()
//                        val activity: MainActivity = context as MainActivity
//                        val myFragment: Fragment = SaleFragment()
//                        val args = Bundle()
//                        args.putString("setting", "setting")
//                        myFragment.arguments = args
//                        activity.getSupportFragmentManager().beginTransaction()
//                            .replace(R.id.frame, myFragment).addToBackStack(null).commit()
                    } else if (msg == "Payment Failed") {
                        d.dismiss()
//                        val intent = Intent(context, MainActivity::class.java)
//                        context.startActivity(intent)
                    } else if (msg == "Connected to tDynamo") {
                        d.dismiss()
                        if (callbackInterface != null) callbackInterface.onCallback("")
                    } else {
                        if (callbackInterface != null) callbackInterface.onCallback("")
                        d.dismiss()
                    }
                }
                d.show()
            } catch (e: java.lang.Exception) {
                Logger.error(
                    " openDialogVoid(final Context context, String msg, final String header",
                    e.message
                )
            }
        }

        fun checkLocation(mContext: Context, sharedPreferences: SharedPreferences) {
            var gps_enabled = false
            var network_enabled = false
            val lm = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            } catch (ex: java.lang.Exception) {
            }
            try {
                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            } catch (ex: java.lang.Exception) {
            }
            if (!gps_enabled && !network_enabled) {
                AppSharedPreferences.writeSp(
                    sharedPreferences,
                    PreferencesKeys.locationStatus,
                    false
                )
            } else {
                AppSharedPreferences.writeSp(
                    sharedPreferences,
                    PreferencesKeys.locationStatus,
                    true
                )
            }
        }
        @SuppressLint("MissingPermission")
        fun enableBluetooth() {
            try {
                val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                bluetoothAdapter.enable()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        @SuppressLint("MissingPermission")
        fun disableBluetooth() {
            try {
                val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                bluetoothAdapter.disable()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }
}