package com.onepay.miura.data

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.onepay.miura.common.PreferencesKeys
import java.lang.Exception

class AppSharedPreferences {

    companion object {

        fun getSharedPreferences(context: Context?): SharedPreferences? {
            return context?.getSharedPreferences(PreferencesKeys.SHARED_PREFS, Context.MODE_PRIVATE)
        }

        fun writeSp(sp: SharedPreferences?, key: String?, value: String?) {
            val edit = sp?.edit()
            edit?.putString(key, value)
            edit?.apply()
        }

        fun writeSp(sp: SharedPreferences?, key: String?, value: Boolean) {
            val edit = sp?.edit()
            edit?.putBoolean(key, value)
            edit?.apply()
        }

        fun writeSp(sp: SharedPreferences?, key: String?, value: Int) {
            val edit = sp?.edit()
            edit?.putInt(key, value)
            edit?.apply()
        }

        fun readString(sp: SharedPreferences?, key: String?): String {
            return sp?.getString(key, "")!!
        }
        fun readInt(sp: SharedPreferences?, key: String?): Int {
            return sp?.getInt(key, 0)!!
        }
        fun readSp(sp: SharedPreferences?, key: String?): Boolean {
            return sp?.getBoolean(key, false)!!
        }
    }

}