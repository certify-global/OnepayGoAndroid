package com.onepay.onepaygo.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.onepay.onepaygo.R
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.data.AppSharedPreferences
import java.util.*

class SplashActivity : AppCompatActivity() {
    var sharedPreferences: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        sharedPreferences = AppSharedPreferences.getSharedPreferences(this)
        setSplashScreenTimer()
    }

    private fun setSplashScreenTimer() {
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                finish()
                if (AppSharedPreferences.readBoolean(sharedPreferences, PreferencesKeys.saveLogin ) && AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.access_token).isNotEmpty())
                    startActivity(Intent(applicationContext, HomeActivity::class.java))
                else
                    startActivity(Intent(applicationContext, LoginActivity::class.java))
            }
        }, 5 * 1000)
    }
}